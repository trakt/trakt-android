@file:OptIn(ExperimentalMaterial3Api::class)

package tv.trakt.trakt.core.userprofile.sections.favorites

import androidx.compose.animation.Crossfade
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
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import tv.trakt.trakt.common.helpers.LoadingState.Done
import tv.trakt.trakt.common.helpers.LoadingState.Idle
import tv.trakt.trakt.common.helpers.LoadingState.Loading
import tv.trakt.trakt.common.helpers.extensions.onClick
import tv.trakt.trakt.common.model.Movie
import tv.trakt.trakt.common.model.Show
import tv.trakt.trakt.core.favorites.model.FavoriteItem
import tv.trakt.trakt.core.profile.sections.favorites.views.FavoriteItemView
import tv.trakt.trakt.core.user.UserCollectionState
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.components.TraktSectionHeader
import tv.trakt.trakt.ui.components.mediacards.skeletons.VerticalMediaSkeletonCard
import tv.trakt.trakt.ui.theme.TraktTheme

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
    onShowClick: (Show) -> Unit = {},
    onMovieClick: (Movie) -> Unit = {},
    onMoreClick: (() -> Unit)? = null,
) {
    Column(
        verticalArrangement = spacedBy(TraktTheme.spacing.mainRowHeaderSpace),
        modifier = modifier,
    ) {
        TraktSectionHeader(
            title = stringResource(R.string.list_title_favorites),
            chevron = onMoreClick != null && !state.items.isNullOrEmpty(),
            modifier = Modifier
                .fillMaxWidth()
                .padding(headerPadding)
                .onClick(enabled = onMoreClick != null && !state.items.isNullOrEmpty()) {
                    onMoreClick?.invoke()
                },
        )

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
            VerticalMediaSkeletonCard(chipRatio = 0.66F)
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
                showMore = false,
                showMediaIcon = true,
                watched = collectionState.isWatched(item.id, item.type, item.airedEpisodes),
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
                modifier = Modifier.animateItem(
                    fadeInSpec = null,
                    fadeOutSpec = null,
                ),
            )
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
