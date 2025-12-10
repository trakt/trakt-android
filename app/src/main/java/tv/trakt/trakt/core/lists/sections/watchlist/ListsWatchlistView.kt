@file:OptIn(ExperimentalMaterial3Api::class)

package tv.trakt.trakt.core.lists.sections.watchlist

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.firebase.Firebase
import com.google.firebase.remoteconfig.remoteConfig
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import org.koin.androidx.compose.koinViewModel
import tv.trakt.trakt.common.firebase.FirebaseConfig.RemoteKey.MOBILE_EMPTY_IMAGE_1
import tv.trakt.trakt.common.firebase.FirebaseConfig.RemoteKey.MOBILE_EMPTY_IMAGE_2
import tv.trakt.trakt.common.helpers.LoadingState.DONE
import tv.trakt.trakt.common.helpers.LoadingState.IDLE
import tv.trakt.trakt.common.helpers.LoadingState.LOADING
import tv.trakt.trakt.common.helpers.extensions.onClick
import tv.trakt.trakt.common.model.MediaType.MOVIE
import tv.trakt.trakt.common.model.MediaType.SHOW
import tv.trakt.trakt.common.model.Movie
import tv.trakt.trakt.common.model.Show
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.core.home.views.HomeEmptyView
import tv.trakt.trakt.core.lists.sections.watchlist.features.context.movies.sheets.WatchlistMovieSheet
import tv.trakt.trakt.core.lists.sections.watchlist.features.context.shows.sheets.WatchlistShowSheet
import tv.trakt.trakt.core.lists.sections.watchlist.model.WatchlistItem
import tv.trakt.trakt.core.lists.sections.watchlist.views.ListsWatchlistItemView
import tv.trakt.trakt.core.main.model.MediaMode
import tv.trakt.trakt.core.main.model.MediaMode.MEDIA
import tv.trakt.trakt.core.main.model.MediaMode.MOVIES
import tv.trakt.trakt.core.main.model.MediaMode.SHOWS
import tv.trakt.trakt.core.user.UserCollectionState
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.components.TraktHeader
import tv.trakt.trakt.ui.components.mediacards.skeletons.VerticalMediaSkeletonCard
import tv.trakt.trakt.ui.theme.TraktTheme

@Composable
internal fun ListsWatchlistView(
    modifier: Modifier = Modifier,
    viewModel: ListsWatchlistViewModel = koinViewModel(),
    headerPadding: PaddingValues,
    contentPadding: PaddingValues,
    onProfileClick: () -> Unit,
    onShowsClick: () -> Unit,
    onShowClick: (TraktId) -> Unit,
    onMoviesClick: () -> Unit,
    onMovieClick: (TraktId) -> Unit,
    onWatchlistClick: () -> Unit,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    var showContextSheet by remember { mutableStateOf<Show?>(null) }
    var movieContextSheet by remember { mutableStateOf<Movie?>(null) }

    LaunchedEffect(state) {
        state.navigateShow?.let {
            viewModel.clearNavigation()
            onShowClick(it)
        }
        state.navigateMovie?.let {
            viewModel.clearNavigation()
            onMovieClick(it)
        }
    }

    ListWatchlistContent(
        state = state,
        modifier = modifier,
        headerPadding = headerPadding,
        contentPadding = contentPadding,
        onShowsClick = onShowsClick,
        onMoviesClick = onMoviesClick,
        onShowClick = { viewModel.navigateToShow(it) },
        onMovieClick = { viewModel.navigateToMovie(it) },
        onShowLongClick = { showContextSheet = it },
        onMovieLongClick = { movieContextSheet = it },
        onProfileClick = onProfileClick,
        onWatchlistClick = {
            if (!state.loading.isLoading) {
                onWatchlistClick()
            }
        },
    )

    WatchlistShowSheet(
        addLocally = true,
        show = showContextSheet,
        watched = showContextSheet?.ids?.trakt?.let {
            state.collection.isWatched(it, SHOW)
        } ?: false,
        onDismiss = { showContextSheet = null },
        onRemoveWatchlist = {
            viewModel.loadData(ignoreErrors = true)
        },
        onAddWatched = {
            viewModel.loadData(ignoreErrors = true)
        },
    )

    WatchlistMovieSheet(
        addLocally = true,
        movie = movieContextSheet,
        watched = movieContextSheet?.ids?.trakt?.let {
            state.collection.isWatched(it, MOVIE)
        } ?: false,
        onDismiss = { movieContextSheet = null },
        onRemoveWatchlist = {
            viewModel.loadData(ignoreErrors = true)
        },
        onAddWatched = {
            viewModel.loadData(ignoreErrors = true)
        },
    )
}

@Composable
internal fun ListWatchlistContent(
    state: ListsWatchlistState,
    modifier: Modifier = Modifier,
    headerPadding: PaddingValues = PaddingValues(),
    contentPadding: PaddingValues = PaddingValues(),
    onShowsClick: () -> Unit = {},
    onShowClick: (Show) -> Unit = {},
    onMoviesClick: () -> Unit = {},
    onMovieClick: (Movie) -> Unit = {},
    onShowLongClick: (Show) -> Unit = {},
    onMovieLongClick: (Movie) -> Unit = {},
    onProfileClick: () -> Unit = {},
    onWatchlistClick: () -> Unit = {},
) {
    Column(
        verticalArrangement = spacedBy(0.dp),
        modifier = modifier,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(headerPadding)
                .onClick(enabled = state.loading == DONE) {
                    onWatchlistClick()
                },
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            TraktHeader(
                title = stringResource(R.string.page_title_watchlist),
                subtitle = stringResource(R.string.text_sort_recently_added),
            )

            if (!state.items.isNullOrEmpty() || state.loading != DONE) {
                Icon(
                    painter = painterResource(R.drawable.ic_chevron_right),
                    contentDescription = null,
                    tint = TraktTheme.colors.textPrimary,
                    modifier = Modifier
                        .size(20.dp)
                        .graphicsLayer {
                            translationX = (4.9).dp.toPx()
                        },
                )
            }
        }

        Spacer(
            Modifier.height(TraktTheme.spacing.mainRowHeaderSpace),
        )

        Crossfade(
            targetState = state.loading,
            animationSpec = tween(200),
        ) { loading ->
            when (loading) {
                IDLE, LOADING -> {
                    ContentLoadingList(
                        visible = loading.isLoading,
                        contentPadding = contentPadding,
                        modifier = Modifier.padding(bottom = 3.75.dp),
                    )
                }

                DONE -> {
                    when {
                        state.error != null -> {
                            Text(
                                text =
                                    "${stringResource(R.string.error_text_unexpected_error_short)}\n\n${state.error}",
                                color = TraktTheme.colors.textSecondary,
                                style = TraktTheme.typography.meta,
                                maxLines = 10,
                                modifier = Modifier.padding(contentPadding),
                            )
                        }

                        state.items?.isEmpty() == true -> {
                            ContentEmptyView(
                                authenticated = (state.user != null),
                                filter = state.filter,
                                onActionClick = {
                                    if (state.user == null) {
                                        onProfileClick()
                                        return@ContentEmptyView
                                    }
                                    when (it) {
                                        MEDIA, SHOWS -> onShowsClick()
                                        MOVIES -> onMoviesClick()
                                    }
                                },
                                modifier = Modifier.padding(contentPadding),
                            )
                        }

                        else -> {
                            ContentList(
                                filter = state.filter,
                                listItems = (state.items ?: emptyList()).toImmutableList(),
                                collection = state.collection,
                                contentPadding = contentPadding,
                                onShowClick = onShowClick,
                                onMovieClick = onMovieClick,
                                onShowLongClick = onShowLongClick,
                                onMovieLongClick = onMovieLongClick,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ContentLoadingList(
    modifier: Modifier = Modifier,
    visible: Boolean = true,
    contentPadding: PaddingValues,
) {
    LazyRow(
        horizontalArrangement = spacedBy(TraktTheme.spacing.mainRowSpace),
        contentPadding = contentPadding,
        modifier = modifier
            .fillMaxWidth()
            .alpha(if (visible) 1F else 0F),
    ) {
        items(count = 6) {
            VerticalMediaSkeletonCard(
                chipRatio = 0.66F,
                chipSpacing = 8.dp,
            )
        }
    }
}

@Composable
private fun ContentList(
    listItems: ImmutableList<WatchlistItem>,
    listState: LazyListState = rememberLazyListState(),
    filter: MediaMode,
    collection: UserCollectionState,
    contentPadding: PaddingValues,
    onShowClick: (Show) -> Unit = {},
    onMovieClick: (Movie) -> Unit = {},
    onShowLongClick: (Show) -> Unit = {},
    onMovieLongClick: (Movie) -> Unit = {},
) {
    val currentList = remember { mutableIntStateOf(listItems.hashCode()) }

    LaunchedEffect(listItems) {
        val hashCode = listItems.hashCode()
        if (currentList.intValue != hashCode) {
            currentList.intValue = hashCode
            listState.animateScrollToItem(0)
        }
    }

    LazyRow(
        state = listState,
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = spacedBy(TraktTheme.spacing.mainRowSpace),
        contentPadding = contentPadding,
    ) {
        items(
            items = listItems,
            key = { it.key },
        ) { item ->
            ListsWatchlistItemView(
                item = item,
                showMediaIcon = (filter == MEDIA),
                watched = collection.isWatched(item.id, item.type),
                onShowClick = {
                    if (item is WatchlistItem.ShowItem && !item.loading) {
                        onShowClick(item.show)
                    }
                },
                onMovieClick = {
                    if (item is WatchlistItem.MovieItem && !item.loading) {
                        onMovieClick(item.movie)
                    }
                },
                onShowLongClick = {
                    if (item is WatchlistItem.ShowItem && !item.loading) {
                        onShowLongClick(item.show)
                    }
                },
                onMovieLongClick = {
                    if (item is WatchlistItem.MovieItem && !item.loading) {
                        onMovieLongClick(item.movie)
                    }
                },
                modifier = Modifier.animateItem(
                    fadeInSpec = null,
                    fadeOutSpec = null,
                ),
            )
        }
    }
}

@Composable
private fun ContentEmptyView(
    filter: MediaMode,
    authenticated: Boolean,
    modifier: Modifier = Modifier,
    onActionClick: (MediaMode) -> Unit = {},
) {
    val imageUrl = remember(filter) {
        val key = when (filter) {
            MEDIA, SHOWS -> MOBILE_EMPTY_IMAGE_1
            MOVIES -> MOBILE_EMPTY_IMAGE_2
        }
        Firebase.remoteConfig.getString(key).ifBlank { null }
    }

    val buttonText = remember(filter, authenticated) {
        if (!authenticated) {
            return@remember R.string.button_text_join_trakt
        }
        when (filter) {
            MEDIA, SHOWS -> R.string.link_text_discover_shows
            MOVIES -> R.string.link_text_discover_movies
        }
    }

    val buttonIcon = remember(authenticated) {
        when {
            authenticated -> R.drawable.ic_search_off
            else -> R.drawable.ic_trakt_icon
        }
    }

    HomeEmptyView(
        text = stringResource(R.string.text_cta_watchlist_unreleased),
        icon = R.drawable.ic_empty_watchlist,
        buttonText = stringResource(buttonText),
        buttonIcon = buttonIcon,
        backgroundImageUrl = imageUrl,
        backgroundImage = if (imageUrl == null) R.drawable.ic_splash_background_2 else null,
        height = (226.25).dp,
        onClick = { onActionClick(filter) },
        modifier = modifier,
    )
}

// Previews

@Preview(
    device = "id:pixel_5",
    showBackground = true,
    backgroundColor = 0xFF131517,
)
@Composable
private fun Preview() {
    TraktTheme {
        ListWatchlistContent(
            state = ListsWatchlistState(
                loading = IDLE,
            ),
        )
    }
}

@Preview(
    device = "id:pixel_5",
    showBackground = true,
    backgroundColor = 0xFF131517,
)
@Composable
private fun Preview2() {
    TraktTheme {
        ListWatchlistContent(
            state = ListsWatchlistState(
                loading = LOADING,
            ),
        )
    }
}

@Preview(
    device = "id:pixel_5",
    showBackground = true,
    backgroundColor = 0xFF131517,
)
@Composable
private fun Preview3() {
    TraktTheme {
        ListWatchlistContent(
            state = ListsWatchlistState(
                loading = DONE,
                items = emptyList<WatchlistItem>().toImmutableList(),
            ),
        )
    }
}
