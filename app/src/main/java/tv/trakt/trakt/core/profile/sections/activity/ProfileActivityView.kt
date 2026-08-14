@file:OptIn(ExperimentalMaterial3Api::class)

package tv.trakt.trakt.core.profile.sections.activity

import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.toImmutableList
import kotlinx.collections.immutable.toImmutableMap
import org.koin.androidx.compose.koinViewModel
import tv.trakt.trakt.common.helpers.LoadingState.Done
import tv.trakt.trakt.common.helpers.LoadingState.Idle
import tv.trakt.trakt.common.helpers.LoadingState.Loading
import tv.trakt.trakt.common.helpers.extensions.onClick
import tv.trakt.trakt.common.model.Comment
import tv.trakt.trakt.common.model.Episode
import tv.trakt.trakt.common.model.Movie
import tv.trakt.trakt.common.model.Show
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.model.reactions.ReactionsSummary
import tv.trakt.trakt.core.comments.features.details.CommentDetailsSheet
import tv.trakt.trakt.core.comments.ui.CommentSkeletonCard
import tv.trakt.trakt.core.profile.sections.activity.model.ProfileActivityFilter
import tv.trakt.trakt.core.profile.sections.activity.model.ProfileActivityFilter.Comments
import tv.trakt.trakt.core.profile.sections.activity.model.ProfileActivityFilter.Ratings
import tv.trakt.trakt.core.profile.sections.activity.model.ProfileCommentItem
import tv.trakt.trakt.core.profile.sections.activity.model.ProfileRatingItem
import tv.trakt.trakt.core.profile.sections.activity.ui.comments.ProfileCommentItemView
import tv.trakt.trakt.core.profile.sections.activity.ui.ratings.ProfileRatingEpisodeItemView
import tv.trakt.trakt.core.profile.sections.activity.ui.ratings.ProfileRatingMovieItemView
import tv.trakt.trakt.core.profile.sections.activity.ui.ratings.ProfileRatingSeasonItemView
import tv.trakt.trakt.core.profile.sections.activity.ui.ratings.ProfileRatingShowItemView
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.components.EmptyHorizontalDoubleHeight
import tv.trakt.trakt.ui.components.EmptyListCard
import tv.trakt.trakt.ui.components.TraktSectionHeader
import tv.trakt.trakt.ui.components.chips.FilterChip
import tv.trakt.trakt.ui.components.chips.FilterChipGroup
import tv.trakt.trakt.ui.components.mediacards.skeletons.EpisodeSkeletonCard
import tv.trakt.trakt.ui.theme.HorizontalImageAspectRatio
import tv.trakt.trakt.ui.theme.TraktTheme

@Composable
internal fun ProfileActivityView(
    modifier: Modifier = Modifier,
    viewModel: ProfileActivityViewModel = koinViewModel(),
    headerPadding: PaddingValues,
    contentPadding: PaddingValues,
    onMovieClick: (TraktId) -> Unit = {},
    onShowClick: (TraktId) -> Unit = {},
    onEpisodeClick: (showId: TraktId, episode: Episode) -> Unit = { _, _ -> },
    onMoreClick: () -> Unit = {},
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    var commentSheet by remember { mutableStateOf<Comment?>(null) }

    LaunchedEffect(
        state.navigateShow,
        state.navigateMovie,
        state.navigateEpisode,
    ) {
        state.navigateShow?.let { onShowClick(it) }
        state.navigateEpisode?.let { onEpisodeClick(it.first, it.second) }
        state.navigateMovie?.let { onMovieClick(it) }
        viewModel.clearNavigation()
    }

    ProfileActivityContent(
        state = state,
        modifier = modifier,
        headerPadding = headerPadding,
        contentPadding = contentPadding,
        onCollapse = viewModel::setCollapsed,
        onRequestReactions = {
            viewModel.loadReactions(it.id)
        },
        onFilterClick = viewModel::setFilter,
        onMovieClick = viewModel::navigateToMovie,
        onShowClick = viewModel::navigateToShow,
        onEpisodeClick = viewModel::navigateToEpisode,
        onCommentClick = {
            commentSheet = it
        },
        onMoreClick = onMoreClick,
    )

    CommentDetailsSheet(
        comment = commentSheet,
        onDeleteComment = {
            viewModel.loadData(ignoreErrors = true)
        },
        onDismiss = {
            commentSheet = null
        },
    )
}

@Composable
internal fun ProfileActivityContent(
    state: ProfileActivityState,
    modifier: Modifier = Modifier,
    headerPadding: PaddingValues = PaddingValues(),
    contentPadding: PaddingValues = PaddingValues(),
    onCollapse: (collapsed: Boolean) -> Unit = {},
    onRequestReactions: (Comment) -> Unit = {},
    onFilterClick: (ProfileActivityFilter) -> Unit = {},
    onMovieClick: (Movie) -> Unit = {},
    onShowClick: (Show) -> Unit = {},
    onEpisodeClick: (Show, Episode) -> Unit = { _, _ -> },
    onCommentClick: (Comment) -> Unit = {},
    onMoreClick: () -> Unit = {},
) {
    var animateCollapse by rememberSaveable { mutableStateOf(false) }

    Column(
        modifier = modifier
            .animateContentSize(
                animationSpec = if (animateCollapse) spring() else snap(),
            ),
    ) {
        val hasItems = !state.ratingItems.isNullOrEmpty() || !state.commentItems.isNullOrEmpty()
        TraktSectionHeader(
            title = stringResource(R.string.list_title_activity),
            chevron = hasItems || state.loading != Done,
            collapsed = state.collapsed ?: false,
            onCollapseClick = {
                animateCollapse = true
                val current = (state.collapsed ?: false)
                onCollapse(!current)
            },
            modifier = Modifier
                .padding(headerPadding)
                .onClick(enabled = state.loading == Done && hasItems) {
                    onMoreClick()
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
                    Idle, Loading -> {
                        ContentLoadingList(
                            visible = loading.isLoading,
                            filter = state.filter,
                            contentPadding = contentPadding,
                        )
                    }

                    Done -> {
                        when {
                            state.error != null -> {
                                Text(
                                    text =
                                        "${
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

                            (state.filter == Ratings && state.ratingItems?.isEmpty() == true) ||
                                (state.filter == Comments && state.commentItems?.isEmpty() == true) -> {
                                EmptyListCard(
                                    height = when {
                                        state.filter == Ratings -> EmptyHorizontalDoubleHeight
                                        else -> TraktTheme.size.commentCardSize
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(contentPadding),
                                )
                            }

                            else -> {
                                when (state.filter) {
                                    Ratings -> {
                                        RatingsContentList(
                                            listItems = (state.ratingItems ?: emptyList()).toImmutableList(),
                                            contentPadding = contentPadding,
                                            onMovieClick = onMovieClick,
                                            onShowClick = onShowClick,
                                            onEpisodeClick = onEpisodeClick,
                                        )
                                    }
                                    Comments -> {
                                        CommentsContentList(
                                            listItems = (state.commentItems ?: emptyList()).toImmutableList(),
                                            listReactions = (state.reactions ?: emptyMap()).toImmutableMap(),
                                            contentPadding = contentPadding,
                                            onRequestReactions = onRequestReactions,
                                            onMovieClick = onMovieClick,
                                            onShowClick = onShowClick,
                                            onEpisodeClick = onEpisodeClick,
                                            onCommentClick = onCommentClick,
                                        )
                                    }
                                }
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
    state: ProfileActivityState,
    onFilterClick: (ProfileActivityFilter) -> Unit,
) {
    FilterChipGroup(
        paddingHorizontal = headerPadding,
        paddingVertical = PaddingValues(top = 13.dp, bottom = 16.dp),
    ) {
        for (filter in ProfileActivityFilter.entries) {
            FilterChip(
                selected = state.filter == filter,
                text = stringResource(filter.displayRes),
                leadingContent = {
                    Icon(
                        painter = painterResource(filter.iconRes),
                        contentDescription = null,
                        tint = TraktTheme.colors.textPrimaryOnAccent,
                        modifier = Modifier
                            .size(19.dp),
                    )
                },
                onClick = { onFilterClick(filter) },
            )
        }
    }
}

@Composable
private fun ContentLoadingList(
    visible: Boolean,
    filter: ProfileActivityFilter,
    contentPadding: PaddingValues,
) {
    LazyRow(
        userScrollEnabled = false,
        horizontalArrangement = spacedBy(TraktTheme.spacing.mainRowSpace),
        contentPadding = contentPadding,
        modifier = Modifier
            .fillMaxWidth()
            .alpha(if (visible) 1F else 0F),
    ) {
        items(count = 6) {
            when (filter) {
                Ratings -> EpisodeSkeletonCard()
                Comments -> CommentSkeletonCard(
                    modifier = Modifier
                        .height(TraktTheme.size.commentCardSize)
                        .aspectRatio(HorizontalImageAspectRatio),
                )
            }
        }
    }
}

@Composable
private fun RatingsContentList(
    listItems: ImmutableList<ProfileRatingItem>,
    listState: LazyListState = rememberLazyListState(),
    contentPadding: PaddingValues,
    onMovieClick: (Movie) -> Unit,
    onShowClick: (Show) -> Unit,
    onEpisodeClick: (Show, Episode) -> Unit,
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
            key = { item -> item.key },
        ) { item ->
            when (item) {
                is ProfileRatingItem.MovieItem -> {
                    ProfileRatingMovieItemView(
                        item = item,
                        onClick = { onMovieClick(item.movie) },
                        modifier = Modifier
                            .animateItem(
                                fadeInSpec = null,
                                fadeOutSpec = null,
                            ),
                    )
                }

                is ProfileRatingItem.ShowItem -> {
                    ProfileRatingShowItemView(
                        item = item,
                        onClick = { onShowClick(item.show) },
                        modifier = Modifier
                            .animateItem(
                                fadeInSpec = null,
                                fadeOutSpec = null,
                            ),
                    )
                }

                is ProfileRatingItem.EpisodeItem -> {
                    ProfileRatingEpisodeItemView(
                        item = item,
                        onClick = { onEpisodeClick(item.show, item.episode) },
                        onShowClick = { onShowClick(item.show) },
                        modifier = Modifier
                            .animateItem(
                                fadeInSpec = null,
                                fadeOutSpec = null,
                            ),
                    )
                }

                is ProfileRatingItem.SeasonItem -> {
                    ProfileRatingSeasonItemView(
                        item = item,
                        onShowClick = { onShowClick(item.show) },
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

@Composable
private fun CommentsContentList(
    listItems: ImmutableList<ProfileCommentItem>,
    listState: LazyListState = rememberLazyListState(),
    listReactions: ImmutableMap<Int, ReactionsSummary>,
    contentPadding: PaddingValues,
    onRequestReactions: (Comment) -> Unit,
    onMovieClick: (Movie) -> Unit,
    onShowClick: (Show) -> Unit,
    onEpisodeClick: (Show, Episode) -> Unit,
    onCommentClick: (Comment) -> Unit,
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
        horizontalArrangement = spacedBy(TraktTheme.spacing.mainRowSpace),
        contentPadding = contentPadding,
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = TraktTheme.spacing.shadowClipSpace),
    ) {
        items(
            items = listItems,
            key = { item -> item.comment.id },
        ) { item ->
            ProfileCommentItemView(
                item = item,
                reactions = listReactions,
                onClick = { onCommentClick(item.comment) },
                onShowClick = { onShowClick(it) },
                onMovieClick = { onMovieClick(it) },
                onEpisodeClick = { show, episode -> onEpisodeClick(show, episode) },
                onRepliesClick = { onCommentClick(item.comment) },
                onDeleteClick = { },
                onRequestReactions = onRequestReactions,
                modifier = Modifier
                    .height(TraktTheme.size.commentCardSize)
                    .aspectRatio(HorizontalImageAspectRatio)
                    .animateItem(
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
        ProfileActivityContent(
            state = ProfileActivityState(
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
        ProfileActivityContent(
            state = ProfileActivityState(
                loading = Done,
                filter = Comments,
            ),
        )
    }
}
