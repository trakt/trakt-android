@file:OptIn(ExperimentalMaterial3Api::class)

package tv.trakt.trakt.core.profile.sections.favorites

import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
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
import tv.trakt.trakt.common.firebase.FirebaseConfig.RemoteKey.MOBILE_EMPTY_IMAGE_3
import tv.trakt.trakt.common.helpers.LoadingState.DONE
import tv.trakt.trakt.common.helpers.LoadingState.IDLE
import tv.trakt.trakt.common.helpers.LoadingState.LOADING
import tv.trakt.trakt.common.helpers.extensions.onClick
import tv.trakt.trakt.common.model.Movie
import tv.trakt.trakt.common.model.Show
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.core.favorites.model.FavoriteItem
import tv.trakt.trakt.core.home.views.HomeEmptyView
import tv.trakt.trakt.core.main.model.MediaMode
import tv.trakt.trakt.core.main.model.MediaMode.MEDIA
import tv.trakt.trakt.core.main.model.MediaMode.MOVIES
import tv.trakt.trakt.core.main.model.MediaMode.SHOWS
import tv.trakt.trakt.core.profile.sections.favorites.context.movie.FavoriteMovieContextSheet
import tv.trakt.trakt.core.profile.sections.favorites.context.show.FavoriteShowContextSheet
import tv.trakt.trakt.core.profile.sections.favorites.views.FavoriteItemView
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.components.MediaModeFilters
import tv.trakt.trakt.ui.components.TraktSectionHeader
import tv.trakt.trakt.ui.components.mediacards.skeletons.VerticalMediaSkeletonCard
import tv.trakt.trakt.ui.theme.TraktTheme

@Composable
internal fun ProfileFavoritesView(
    modifier: Modifier = Modifier,
    viewModel: ProfileFavoritesViewModel = koinViewModel(),
    headerPadding: PaddingValues,
    contentPadding: PaddingValues,
    onShowClick: (TraktId) -> Unit,
    onMovieClick: (TraktId) -> Unit,
    onMoreClick: () -> Unit,
    onShowsClick: () -> Unit,
    onMoviesClick: () -> Unit,
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

    ProfileFavoritesContent(
        state = state,
        modifier = modifier,
        headerPadding = headerPadding,
        contentPadding = contentPadding,
        onCollapse = viewModel::setCollapsed,
        onFilterClick = viewModel::setFilter,
        onShowClick = { viewModel.navigateToShow(it) },
        onMovieClick = { viewModel.navigateToMovie(it) },
        onShowLongClick = { showContextSheet = it },
        onMovieLongClick = { movieContextSheet = it },
        onFavoritesClick = {
            if (state.loading == DONE && !state.items.isNullOrEmpty()) {
                onMoreClick()
            }
        },
        onShowsClick = onShowsClick,
        onMoviesClick = onMoviesClick,
    )

    FavoriteShowContextSheet(
        show = showContextSheet,
        onDismiss = { showContextSheet = null },
    )

    FavoriteMovieContextSheet(
        movie = movieContextSheet,
        onDismiss = { movieContextSheet = null },
    )
}

@Composable
internal fun ProfileFavoritesContent(
    state: ProfileFavoritesState,
    modifier: Modifier = Modifier,
    headerPadding: PaddingValues = PaddingValues(),
    contentPadding: PaddingValues = PaddingValues(),
    onCollapse: (collapsed: Boolean) -> Unit = {},
    onFilterClick: (MediaMode) -> Unit = {},
    onShowClick: (Show) -> Unit = {},
    onMovieClick: (Movie) -> Unit = {},
    onShowLongClick: (Show) -> Unit = {},
    onMovieLongClick: (Movie) -> Unit = {},
    onFavoritesClick: () -> Unit = {},
    onShowsClick: () -> Unit = {},
    onMoviesClick: () -> Unit = {},
) {
    var animateCollapse by rememberSaveable { mutableStateOf(false) }

    Column(
        verticalArrangement = spacedBy(TraktTheme.spacing.mainRowHeaderSpace),
        modifier = modifier
            .animateContentSize(
                animationSpec = if (animateCollapse) spring() else snap(),
            ),
    ) {
        TraktSectionHeader(
            title = stringResource(R.string.list_title_favorites),
            subtitle = stringResource(R.string.text_sort_recently_added),
            chevron = !state.items.isNullOrEmpty() || state.loading != DONE,
            collapsed = state.collapsed ?: false,
            onCollapseClick = {
                animateCollapse = true
                val current = (state.collapsed ?: false)
                onCollapse(!current)
            },
            modifier = Modifier
                .padding(headerPadding)
                .onClick(enabled = state.loading == DONE) {
                    onFavoritesClick()
                },
        )

        if (state.collapsed != true) {
            ContentFilters(
                state = state,
                headerPadding = headerPadding,
                onFilterClick = onFilterClick,
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
                                        "${stringResource(
                                            R.string.error_text_unexpected_error_short,
                                        )}\n\n${state.error}",
                                    color = TraktTheme.colors.textSecondary,
                                    style = TraktTheme.typography.meta,
                                    maxLines = 10,
                                    modifier = Modifier.padding(contentPadding),
                                )
                            }

                            state.items?.isEmpty() == true -> {
                                ContentEmptyView(
                                    filter = state.filter,
                                    onShowsClick = onShowsClick,
                                    onMoviesClick = onMoviesClick,
                                    modifier = Modifier.padding(contentPadding),
                                )
                            }

                            else -> {
                                ContentList(
                                    filter = state.filter,
                                    listItems = (state.items ?: emptyList()).toImmutableList(),
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
}

@Composable
private fun ContentFilters(
    headerPadding: PaddingValues,
    state: ProfileFavoritesState,
    onFilterClick: (MediaMode) -> Unit,
) {
    MediaModeFilters(
        selected = state.filter,
        onClick = onFilterClick,
        paddingHorizontal = headerPadding,
        paddingVertical = PaddingValues(top = 13.dp, bottom = 15.dp),
    )
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
        userScrollEnabled = false,
        modifier = modifier
            .fillMaxWidth()
            .alpha(if (visible) 1F else 0F),
    ) {
        items(count = 6) {
            VerticalMediaSkeletonCard(chipRatio = 0.66F)
        }
    }
}

@Composable
private fun ContentList(
    listItems: ImmutableList<FavoriteItem>,
    listState: LazyListState = rememberLazyListState(),
    filter: MediaMode?,
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
            FavoriteItemView(
                item = item,
                showMediaIcon = (filter == MEDIA),
                onShowClick = {
                    if (item is FavoriteItem.ShowItem && !item.loading) {
                        onShowClick(item.show)
                    }
                },
                onMovieClick = {
                    if (item is FavoriteItem.MovieItem && !item.loading) {
                        onMovieClick(item.movie)
                    }
                },
                onShowLongClick = {
                    if (item is FavoriteItem.ShowItem && !item.loading) {
                        onShowLongClick(item.show)
                    }
                },
                onMovieLongClick = {
                    if (item is FavoriteItem.MovieItem && !item.loading) {
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
    filter: MediaMode?,
    modifier: Modifier = Modifier,
    onShowsClick: () -> Unit,
    onMoviesClick: () -> Unit,
) {
    val height = 219.dp

    val imageUrls = remember {
        val remoteConfig = Firebase.remoteConfig
        buildList {
            add(remoteConfig.getString(MOBILE_EMPTY_IMAGE_1).ifBlank { null })
            add(remoteConfig.getString(MOBILE_EMPTY_IMAGE_2).ifBlank { null })
            add(remoteConfig.getString(MOBILE_EMPTY_IMAGE_3).ifBlank { null })
        }
    }

    when (filter) {
        MEDIA -> {
            HomeEmptyView(
                text = stringResource(R.string.text_cta_favorites),
                icon = R.drawable.ic_empty_watchlist,
                height = height,
                buttonText = stringResource(R.string.link_text_discover_shows),
                backgroundImageUrl = imageUrls.getOrNull(0),
                backgroundImage = if (imageUrls.getOrNull(0) == null) R.drawable.ic_splash_background else null,
                onClick = onShowsClick,
                modifier = modifier,
            )
        }

        SHOWS -> {
            HomeEmptyView(
                text = stringResource(R.string.text_cta_favorites_shows),
                icon = R.drawable.ic_empty_watchlist,
                height = height,
                buttonText = stringResource(R.string.link_text_discover_shows),
                backgroundImageUrl = imageUrls.getOrNull(2),
                backgroundImage = if (imageUrls.getOrNull(2) == null) R.drawable.ic_splash_background_2 else null,
                onClick = onShowsClick,
                modifier = modifier,
            )
        }

        MOVIES -> {
            HomeEmptyView(
                text = stringResource(R.string.text_cta_favorites_movies),
                icon = R.drawable.ic_empty_watchlist,
                height = height,
                buttonText = stringResource(R.string.link_text_discover_movies),
                backgroundImageUrl = imageUrls.getOrNull(1),
                backgroundImage = if (imageUrls.getOrNull(1) == null) R.drawable.ic_splash_background_2 else null,
                onClick = onMoviesClick,
                modifier = modifier,
            )
        }

        else -> {
            Unit
        }
    }
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
        ProfileFavoritesContent(
            state = ProfileFavoritesState(
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
        ProfileFavoritesContent(
            state = ProfileFavoritesState(
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
        ProfileFavoritesContent(
            state = ProfileFavoritesState(
                loading = DONE,
                items = emptyList<FavoriteItem>().toImmutableList(),
            ),
        )
    }
}
