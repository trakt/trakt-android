@file:OptIn(ExperimentalFoundationApi::class)
@file:Suppress("FunctionName")

package tv.trakt.trakt.core.summary.shows.features.seasons.all.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement.Absolute.spacedBy
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil3.ColorImage
import coil3.annotation.ExperimentalCoilApi
import coil3.compose.AsyncImagePreviewHandler
import coil3.compose.LocalAsyncImagePreviewHandler
import kotlinx.collections.immutable.toImmutableList
import tv.trakt.trakt.common.helpers.extensions.EmptyImmutableList
import tv.trakt.trakt.common.helpers.extensions.onClick
import tv.trakt.trakt.common.helpers.preview.PreviewData
import tv.trakt.trakt.common.model.Comment
import tv.trakt.trakt.common.model.User
import tv.trakt.trakt.common.model.reactions.Reaction
import tv.trakt.trakt.core.comments.model.CommentsFilter
import tv.trakt.trakt.core.comments.ui.CommentCard
import tv.trakt.trakt.core.comments.ui.CommentSkeletonCard
import tv.trakt.trakt.core.summary.shows.features.seasons.all.AllShowSeasonsState
import tv.trakt.trakt.core.summary.shows.features.seasons.model.ShowSeasons
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.components.EmptyListCard
import tv.trakt.trakt.ui.components.EmptyVerticalPanelHeight
import tv.trakt.trakt.ui.components.TraktSectionHeader
import tv.trakt.trakt.ui.components.chips.FilterChip
import tv.trakt.trakt.ui.components.chips.FilterChipGroup
import tv.trakt.trakt.ui.theme.TraktTheme

internal fun LazyListScope.SeasonReviewsSection(
    contentPadding: PaddingValues,
    state: AllShowSeasonsState,
    onFilterClick: ((CommentsFilter) -> Unit),
    onRepliesClick: ((Comment) -> Unit),
    onRequestReactions: ((Comment) -> Unit),
    onReactionClick: ((Reaction, Comment) -> Unit),
    onNewCommentClick: (() -> Unit)? = null,
    onReplyClick: ((Comment) -> Unit)? = null,
    onReplyUserClick: ((Comment, User) -> Unit)? = null,
    onDeleteCommentClick: ((Comment) -> Unit)? = null,
    onDeleteReplyClick: ((Comment) -> Unit)? = null,
) {
    item(
        key = "season_reviews_header",
    ) {
        Column(
            verticalArrangement = spacedBy(12.dp),
            modifier = Modifier
                .padding(contentPadding)
                .padding(bottom = 11.dp)
                .graphicsLayer {
                    translationY = -2.dp.toPx()
                },
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                TraktSectionHeader(
                    title = stringResource(R.string.list_title_comments),
                    chevron = false,
                    collapsable = false,
                    modifier = Modifier.weight(1F),
                )

                if (state.user != null) {
                    Icon(
                        painter = painterResource(R.drawable.ic_comment_plus),
                        contentDescription = stringResource(R.string.dialog_title_comment),
                        tint = TraktTheme.colors.textPrimary,
                        modifier = Modifier
                            .size(20.dp)
                            .graphicsLayer {
                                translationY = 2.dp.toPx()
                            }
                            .onClick {
                                onNewCommentClick?.invoke()
                            },
                    )
                }
            }
            FilterChipGroup(
                paddingVertical = PaddingValues(bottom = 3.dp),
            ) {
                for (filter in CommentsFilter.entries) {
                    FilterChip(
                        selected = state.commentsMode == filter,
                        text = stringResource(filter.displayRes),
                        leadingContent = {
                            Icon(
                                painter = painterResource(filter.iconRes),
                                contentDescription = null,
                                tint = TraktTheme.colors.textPrimaryOnAccent,
                                modifier = Modifier.size(19.dp),
                            )
                        },
                        onClick = {
                            onFilterClick(filter)
                        },
                    )
                }
            }
        }
    }

    if (state.items.isSeasonCommentsLoading) {
        items(count = 3) {
            CommentSkeletonCard(
                corner = 20.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(TraktTheme.size.commentCardSize)
                    .padding(contentPadding)
                    .padding(bottom = 16.dp)
                    .animateItem(
                        fadeInSpec = null,
                        fadeOutSpec = null,
                    ),
            )
        }
    } else {
        val comments = state.items.selectedSeasonComments
        if (comments.isNotEmpty()) {
            items(
                items = comments,
                key = { it.id },
            ) { comment ->
                val isUserComment = comment.user.ids.trakt == state.user?.ids?.trakt

                CommentCard(
                    user = state.user,
                    comment = comment,
                    replies = state.items.selectedSeasonReplies[comment.id] ?: EmptyImmutableList,
                    repliesLoading = state.items.selectedSeasonRepliesLoading.contains(comment.id),
                    reactions = state.commentReactions,
                    userReactions = state.userReactions,
                    replyEnabled = state.user != null && !isUserComment,
                    repliesButtonEnabled = true,
                    repliesCountEnabled = false,
                    onRequestReactions = { onRequestReactions(it) },
                    onReactionClick = { reaction, target -> onReactionClick(reaction, target) },
                    onRepliesClick = { onRepliesClick(comment) },
                    onReplyClick = { onReplyClick?.invoke(it) },
                    onReplyUserClick = { target, user -> onReplyUserClick?.invoke(target, user) },
                    onDeleteClick = { onDeleteCommentClick?.invoke(comment) },
                    onDeleteReplyClick = { onDeleteReplyClick?.invoke(it) },
                    modifier = Modifier
                        .padding(contentPadding)
                        .padding(bottom = 16.dp)
                        .animateItem(
                            fadeInSpec = null,
                            fadeOutSpec = null,
                        ),
                )
            }
        } else {
            item {
                EmptyListCard(
                    modifier = Modifier
                        .height(EmptyVerticalPanelHeight)
                        .padding(contentPadding)
                        .padding(bottom = 12.dp)
                        .animateItem(
                            fadeInSpec = null,
                            fadeOutSpec = null,
                        ),
                )
            }
        }
    }
}

@OptIn(ExperimentalCoilApi::class)
@Preview(
    device = "id:pixel_5",
    showBackground = true,
    backgroundColor = 0xFF131517,
)
@Composable
private fun PreviewSeasonReviewsSection() {
    TraktTheme {
        val previewHandler = AsyncImagePreviewHandler {
            ColorImage(Color.LightGray.toArgb())
        }
        val contentPadding = PaddingValues(
            horizontal = TraktTheme.spacing.mainPageHorizontalSpace,
        )
        val comments = listOf(
            PreviewData.comment1.copy(comment = "One of the best seasons yet."),
            PreviewData.comment1.copy(id = 2, userRating = 8, comment = "Slow start, strong finish."),
        ).toImmutableList()

        CompositionLocalProvider(LocalAsyncImagePreviewHandler provides previewHandler) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .background(TraktTheme.colors.backgroundPrimary),
            ) {
                SeasonReviewsSection(
                    contentPadding = contentPadding,
                    state = AllShowSeasonsState(
                        show = PreviewData.show1,
                        user = PreviewData.user1,
                        commentsMode = CommentsFilter.Popular,
                        items = ShowSeasons(
                            selectedSeason = PreviewData.season1.copy(number = 1),
                            selectedSeasonComments = comments,
                        ),
                    ),
                    onFilterClick = {},
                    onRepliesClick = {},
                    onRequestReactions = {},
                    onReactionClick = { _, _ -> },
                )
            }
        }
    }
}
