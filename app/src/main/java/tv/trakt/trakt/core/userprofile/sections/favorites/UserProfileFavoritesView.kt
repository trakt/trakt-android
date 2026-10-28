package tv.trakt.trakt.core.userprofile.sections.favorites

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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import tv.trakt.trakt.common.core.favorites.FavoriteItem
import tv.trakt.trakt.common.core.user.UserCollectionState
import tv.trakt.trakt.common.helpers.LoadingState.Done
import tv.trakt.trakt.common.helpers.LoadingState.Idle
import tv.trakt.trakt.common.helpers.LoadingState.Loading
import tv.trakt.trakt.common.helpers.extensions.onClick
import tv.trakt.trakt.common.helpers.preview.PreviewData
import tv.trakt.trakt.common.model.Movie
import tv.trakt.trakt.common.model.Show
import tv.trakt.trakt.core.profile.sections.favorites.views.FavoriteItemView
import tv.trakt.trakt.core.profile.sections.favorites.views.FavoritesYearLabel
import tv.trakt.trakt.core.profile.sections.favorites.views.toFavoritesYearHeaders
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.components.TraktSectionHeader
import tv.trakt.trakt.ui.components.mediacards.skeletons.VerticalMediaSkeletonCard
import tv.trakt.trakt.ui.theme.TraktTheme
import java.time.Instant

@Composable
internal fun UserProfileFavoritesView(
    modifier: Modifier = Modifier,
    viewModel: UserProfileFavoritesViewModel,
    headerPadding: PaddingValues,
    contentPadding: PaddingValues,
    onShowClick: ((Show) -> Unit)? = null,
    onMovieClick: ((Movie) -> Unit)? = null,
    onMoreClick: (() -> Unit)? = null,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    UserProfileFavoritesContent(
        state = state,
        modifier = modifier,
        headerPadding = headerPadding,
        contentPadding = contentPadding,
        onCollapse = viewModel::setCollapsed,
        onShowClick = { onShowClick?.invoke(it) },
        onMovieClick = { onMovieClick?.invoke(it) },
        onMoreClick = onMoreClick,
    )
}

@Composable
internal fun UserProfileFavoritesContent(
    state: UserProfileFavoritesState,
    modifier: Modifier = Modifier,
    headerPadding: PaddingValues = PaddingValues(),
    contentPadding: PaddingValues = PaddingValues(),
    onCollapse: (Boolean) -> Unit = {},
    onShowClick: (Show) -> Unit = {},
    onMovieClick: (Movie) -> Unit = {},
    onMoreClick: (() -> Unit)? = null,
) {
    var animateCollapse by rememberSaveable { mutableStateOf(false) }

    Column(
        verticalArrangement = spacedBy(3.dp),
        modifier = modifier
            .animateContentSize(
                animationSpec = if (animateCollapse) spring() else snap(),
            ),
    ) {
        TraktSectionHeader(
            title = stringResource(R.string.list_title_favorites),
            chevron = onMoreClick != null && !state.items.isNullOrEmpty(),
            collapsed = state.collapsed ?: false,
            onCollapseClick = {
                animateCollapse = true
                val current = (state.collapsed ?: false)
                onCollapse(!current)
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(headerPadding)
                .onClick(enabled = onMoreClick != null && !state.items.isNullOrEmpty()) {
                    onMoreClick?.invoke()
                },
        )

        if (state.collapsed != true) {
            Crossfade(
                targetState = state.loading,
                animationSpec = tween(200),
            ) { loading ->
                when (loading) {
                    Idle, Loading -> {
                        ContentLoadingList(
                            visible = loading.isLoading,
                            contentPadding = contentPadding,
                        )
                    }

                    Done -> {
                        when {
                            state.error != null -> {
                                Text(
                                    text = "${
                                        stringResource(
                                            R.string.error_text_unexpected_error_short,
                                        )
                                    }\n\n${state.error}",
                                    color = TraktTheme.colors.textSecondary,
                                    style = TraktTheme.typography.meta,
                                    maxLines = 10,
                                    modifier = Modifier.padding(contentPadding),
                                )
                            }

                            state.items?.isEmpty() == true -> {
                                Text(
                                    text = stringResource(R.string.list_placeholder_empty),
                                    color = TraktTheme.colors.textSecondary,
                                    style = TraktTheme.typography.heading6,
                                    modifier = Modifier.padding(contentPadding),
                                )
                            }

                            else -> {
                                ContentList(
                                    listItems = (state.items ?: emptyList()).toImmutableList(),
                                    collectionState = state.collection,
                                    contentPadding = contentPadding,
                                    onShowClick = onShowClick,
                                    onMovieClick = onMovieClick,
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
        items(count = 12) {
            Column {
                FavoritesYearLabel(year = null)
                VerticalMediaSkeletonCard(chipRatio = 0.66F)
            }
        }
    }
}

@Composable
private fun ContentList(
    listItems: ImmutableList<FavoriteItem>,
    collectionState: UserCollectionState,
    listState: LazyListState = rememberLazyListState(),
    contentPadding: PaddingValues,
    onShowClick: (Show) -> Unit = {},
    onMovieClick: (Movie) -> Unit = {},
) {
    val yearHeaders = remember(listItems) { listItems.toFavoritesYearHeaders() }

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
            Column(
                verticalArrangement = spacedBy(6.dp),
                modifier = Modifier.animateItem(
                    fadeInSpec = null,
                    fadeOutSpec = null,
                ),
            ) {
                FavoritesYearLabel(year = yearHeaders[item.key])
                FavoriteItemView(
                    item = item,
                    showMore = false,
                    showMediaIcon = true,
                    watched = collectionState.isWatched(item.id, item.type, item.airedEpisodes),
                    watching = collectionState.isWatching(item.id, item.type, item.airedEpisodes),
                    watchlist = collectionState.isWatchlist(item.id, item.type),
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
                    onShowLongClick = {},
                    onMovieLongClick = {},
                )
            }
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
        UserProfileFavoritesContent(
            state = UserProfileFavoritesState(
                loading = Idle,
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
        UserProfileFavoritesContent(
            state = UserProfileFavoritesState(
                loading = Loading,
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
private fun Preview4() {
    TraktTheme {
        UserProfileFavoritesContent(
            state = UserProfileFavoritesState(
                loading = Done,
                items = listOf(
                    FavoriteItem.ShowItem(
                        show = PreviewData.show1,
                        rank = 1,
                        listedAt = Instant.parse("2026-03-02T10:00:00Z"),
                    ),
                    FavoriteItem.MovieItem(
                        movie = PreviewData.movie1,
                        rank = 2,
                        listedAt = Instant.parse("2025-11-20T10:00:00Z"),
                    ),
                    FavoriteItem.ShowItem(
                        show = PreviewData.show2,
                        rank = 3,
                        listedAt = Instant.parse("2025-06-14T10:00:00Z"),
                    ),
                    FavoriteItem.MovieItem(
                        movie = PreviewData.movie2,
                        rank = 4,
                        listedAt = Instant.parse("2024-01-05T10:00:00Z"),
                    ),
                ).toImmutableList(),
            ),
        )
    }
}
