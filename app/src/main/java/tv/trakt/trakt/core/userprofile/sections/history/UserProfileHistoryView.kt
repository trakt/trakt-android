@file:OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)

package tv.trakt.trakt.core.userprofile.sections.history

import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
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
import tv.trakt.trakt.common.model.Episode
import tv.trakt.trakt.common.model.Movie
import tv.trakt.trakt.common.model.Show
import tv.trakt.trakt.core.home.sections.activity.model.HomeActivityItem
import tv.trakt.trakt.core.home.sections.activity.views.ActivityEpisodeItemView
import tv.trakt.trakt.core.home.sections.activity.views.ActivityMovieItemView
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.components.TraktSectionHeader
import tv.trakt.trakt.ui.components.mediacards.skeletons.EpisodeSkeletonCard
import tv.trakt.trakt.ui.theme.TraktTheme

@Composable
internal fun UserProfileHistoryView(
    modifier: Modifier = Modifier,
    viewModel: UserProfileHistoryViewModel?,
    headerPadding: PaddingValues,
    contentPadding: PaddingValues,
    onShowClick: ((Show) -> Unit)? = null,
    onEpisodeClick: ((Show, Episode) -> Unit)? = null,
    onMovieClick: ((Movie) -> Unit)? = null,
    onMoreClick: () -> Unit = {},
) {
    val collectedState = viewModel?.state?.collectAsStateWithLifecycle()?.value
    val state = when {
        collectedState == null -> UserProfileHistoryState(loading = Loading)
        collectedState.loading == Idle -> collectedState.copy(loading = Loading)
        else -> collectedState
    }

    UserProfileHistoryContent(
        state = state,
        modifier = modifier,
        headerPadding = headerPadding,
        contentPadding = contentPadding,
        onCollapse = { viewModel?.setCollapsed(it) },
        onShowClick = { onShowClick?.invoke(it) },
        onEpisodeClick = { show, episode -> onEpisodeClick?.invoke(show, episode) },
        onMovieClick = { onMovieClick?.invoke(it) },
        onMoreClick = {
            if (state.loading.isLoading) return@UserProfileHistoryContent
            onMoreClick()
        },
    )
}

@Composable
internal fun UserProfileHistoryContent(
    state: UserProfileHistoryState,
    modifier: Modifier = Modifier,
    headerPadding: PaddingValues = PaddingValues(),
    contentPadding: PaddingValues = PaddingValues(),
    onCollapse: (Boolean) -> Unit = {},
    onShowClick: (Show) -> Unit = {},
    onEpisodeClick: (Show, Episode) -> Unit = { _, _ -> },
    onMovieClick: (Movie) -> Unit = {},
    onMoreClick: () -> Unit = {},
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
            title = stringResource(R.string.list_title_history),
            chevron = !state.items.isNullOrEmpty() || state.loading != Done,
            collapsed = state.collapsed ?: false,
            onCollapseClick = {
                animateCollapse = true
                val current = (state.collapsed ?: false)
                onCollapse(!current)
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(headerPadding)
                .onClick(enabled = state.loading == Done) {
                    onMoreClick()
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
                                    text = "${stringResource(
                                        R.string.error_text_unexpected_error_short,
                                    )}\n\n${state.error}",
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
                                    contentPadding = contentPadding,
                                    onShowClick = onShowClick,
                                    onEpisodeClick = onEpisodeClick,
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
        items(count = 6) {
            EpisodeSkeletonCard()
        }
    }
}

@Composable
private fun ContentList(
    listItems: ImmutableList<HomeActivityItem>,
    listState: LazyListState = rememberLazyListState(),
    contentPadding: PaddingValues,
    onShowClick: (Show) -> Unit,
    onEpisodeClick: (Show, Episode) -> Unit,
    onMovieClick: (Movie) -> Unit,
) {
    LazyRow(
        state = listState,
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = spacedBy(TraktTheme.spacing.mainRowSpace),
        contentPadding = contentPadding,
    ) {
        items(
            items = listItems,
            key = { it.id },
        ) { item ->
            when (item) {
                is HomeActivityItem.EpisodeItem -> {
                    ActivityEpisodeItemView(
                        item = item,
                        moreButton = false,
                        onClick = { onEpisodeClick(item.show, item.episode) },
                        onShowClick = { onShowClick(item.show) },
                        modifier = Modifier
                            .animateItem(
                                fadeInSpec = null,
                                fadeOutSpec = null,
                            ),
                    )
                }

                is HomeActivityItem.MovieItem -> {
                    ActivityMovieItemView(
                        item = item,
                        moreButton = false,
                        onClick = { onMovieClick(item.movie) },
                        modifier = Modifier
                            .animateItem(
                                fadeInSpec = null,
                                fadeOutSpec = null,
                            ),
                    )
                }
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
        UserProfileHistoryContent(
            state = UserProfileHistoryState(
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
        UserProfileHistoryContent(
            state = UserProfileHistoryState(
                loading = Loading,
            ),
        )
    }
}
