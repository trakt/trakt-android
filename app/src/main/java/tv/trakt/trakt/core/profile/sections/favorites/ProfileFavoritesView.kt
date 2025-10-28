@file:OptIn(ExperimentalMaterial3Api::class)

package tv.trakt.trakt.core.profile.sections.favorites

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChipDefaults
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
import tv.trakt.trakt.common.firebase.FirebaseConfig.RemoteKey.MOBILE_EMPTY_IMAGE_3
import tv.trakt.trakt.common.helpers.LoadingState.DONE
import tv.trakt.trakt.common.helpers.LoadingState.IDLE
import tv.trakt.trakt.common.helpers.LoadingState.LOADING
import tv.trakt.trakt.common.helpers.extensions.onClick
import tv.trakt.trakt.common.model.Movie
import tv.trakt.trakt.common.model.Show
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.core.home.views.HomeEmptyView
import tv.trakt.trakt.core.lists.model.ListsMediaFilter
import tv.trakt.trakt.core.lists.model.ListsMediaFilter.MEDIA
import tv.trakt.trakt.core.lists.model.ListsMediaFilter.MOVIES
import tv.trakt.trakt.core.lists.model.ListsMediaFilter.SHOWS
import tv.trakt.trakt.core.movies.ui.context.sheet.MovieContextSheet
import tv.trakt.trakt.core.profile.model.FavoriteItem
import tv.trakt.trakt.core.profile.sections.favorites.views.FavoriteItemView
import tv.trakt.trakt.core.shows.ui.context.sheet.ShowContextSheet
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.components.FilterChip
import tv.trakt.trakt.ui.components.FilterChipGroup
import tv.trakt.trakt.ui.components.TraktHeader
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
        onFilterClick = viewModel::setFilter,
        onShowClick = { viewModel.navigateToShow(it) },
        onMovieClick = { viewModel.navigateToMovie(it) },
        onShowLongClick = { /* TODO */ },
        onMovieLongClick = { /* TODO */ },
        onFavoritesClick = {
            if (state.loading == DONE && !state.items.isNullOrEmpty()) {
                onMoreClick()
            }
        },
    )

    ShowContextSheet(
        show = showContextSheet,
        onDismiss = { showContextSheet = null },
    )

    MovieContextSheet(
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
    onFilterClick: (ListsMediaFilter) -> Unit = {},
    onShowClick: (Show) -> Unit = {},
    onMovieClick: (Movie) -> Unit = {},
    onShowLongClick: (Show) -> Unit = {},
    onMovieLongClick: (Movie) -> Unit = {},
    onFavoritesClick: () -> Unit = {},
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
                    onFavoritesClick()
                },
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            TraktHeader(
                title = stringResource(R.string.page_title_favorites),
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
                                filter = state.filter,
                                onShowsClick = { },
                                onMoviesClick = { },
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

@Composable
private fun ContentFilters(
    headerPadding: PaddingValues,
    state: ProfileFavoritesState,
    onFilterClick: (ListsMediaFilter) -> Unit,
) {
    FilterChipGroup(
        paddingHorizontal = headerPadding,
    ) {
        for (filter in ListsMediaFilter.entries) {
            FilterChip(
                selected = state.filter == filter,
                text = stringResource(filter.displayRes),
                leadingContent = {
                    Icon(
                        painter = painterResource(filter.iconRes),
                        contentDescription = null,
                        tint = TraktTheme.colors.textPrimary,
                        modifier = Modifier.size(FilterChipDefaults.IconSize),
                    )
                },
                onClick = { onFilterClick(filter) },
            )
        }
    }
}

@Composable
private fun ContentLoadingList(
    visible: Boolean = true,
    contentPadding: PaddingValues,
) {
    LazyRow(
        horizontalArrangement = spacedBy(TraktTheme.spacing.mainRowSpace),
        contentPadding = contentPadding,
        userScrollEnabled = false,
        modifier = Modifier
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
    filter: ListsMediaFilter,
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
    filter: ListsMediaFilter,
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
                buttonText = stringResource(R.string.button_label_browse_shows),
                backgroundImageUrl = imageUrls.getOrNull(0),
                backgroundImage = if (imageUrls.getOrNull(0) == null) R.drawable.ic_splash_background else null,
                onClick = onShowsClick,
                modifier = modifier,
            )
        }
        SHOWS -> HomeEmptyView(
            text = stringResource(R.string.text_cta_favorites_shows),
            icon = R.drawable.ic_empty_watchlist,
            height = height,
            buttonText = stringResource(R.string.button_label_browse_shows),
            backgroundImageUrl = imageUrls.getOrNull(2),
            backgroundImage = if (imageUrls.getOrNull(2) == null) R.drawable.ic_splash_background_2 else null,
            onClick = onShowsClick,
            modifier = modifier,
        )
        MOVIES -> HomeEmptyView(
            text = stringResource(R.string.text_cta_favorites_movies),
            icon = R.drawable.ic_empty_watchlist,
            height = height,
            buttonText = stringResource(R.string.button_label_browse_movies),
            backgroundImageUrl = imageUrls.getOrNull(1),
            backgroundImage = if (imageUrls.getOrNull(1) == null) R.drawable.ic_splash_background_2 else null,
            onClick = onMoviesClick,
            modifier = modifier,
        )
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
