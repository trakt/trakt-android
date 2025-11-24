@file:OptIn(ExperimentalMaterial3Api::class)

package tv.trakt.trakt.core.comments.features.details

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.ColorImage
import coil3.annotation.ExperimentalCoilApi
import coil3.compose.AsyncImage
import coil3.compose.AsyncImagePreviewHandler
import coil3.compose.LocalAsyncImagePreviewHandler
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.toImmutableList
import kotlinx.collections.immutable.toImmutableMap
import kotlinx.coroutines.launch
import tv.trakt.trakt.common.helpers.LoadingState
import tv.trakt.trakt.common.helpers.LoadingState.DONE
import tv.trakt.trakt.common.helpers.extensions.longDateTimeFormat
import tv.trakt.trakt.common.helpers.extensions.onClick
import tv.trakt.trakt.common.helpers.preview.PreviewData
import tv.trakt.trakt.common.model.Comment
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.model.User
import tv.trakt.trakt.common.model.reactions.Reaction
import tv.trakt.trakt.common.model.reactions.ReactionsSummary
import tv.trakt.trakt.common.model.toTraktId
import tv.trakt.trakt.common.ui.theme.colors.Shade800
import tv.trakt.trakt.core.comments.features.deletecomment.DeleteCommentSheet
import tv.trakt.trakt.core.comments.ui.CommentReplyCard
import tv.trakt.trakt.core.comments.ui.CommentSkeletonCard
import tv.trakt.trakt.core.reactions.ui.ReactionsSummaryChip
import tv.trakt.trakt.core.reactions.ui.ReactionsToolTip
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.theme.TraktTheme

@Composable
internal fun CommentDetailsView(
    viewModel: CommentDetailsViewModel,
    modifier: Modifier = Modifier,
    onDeleteComment: (commentId: TraktId) -> Unit = {},
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    var deleteCommentSheet by remember { mutableStateOf<Comment?>(null) }

    CommentDetailsViewContent(
        state = state,
        modifier = modifier,
        onReplyLoaded = {
            viewModel.loadReactions(it.id)
        },
        onReactionClick = { reaction, comment ->
            viewModel.setReaction(reaction, comment.id)
        },
        onDeleteClick = { comment ->
            deleteCommentSheet = comment
        },
    )

    DeleteCommentSheet(
        active = deleteCommentSheet != null,
        commentId = deleteCommentSheet?.id?.toTraktId(),
        onDeleted = onDeleteComment,
        onDismiss = {
            deleteCommentSheet = null
        },
    )
}

@Composable
private fun CommentDetailsViewContent(
    state: CommentDetailsState,
    modifier: Modifier = Modifier,
    onReplyLoaded: ((Comment) -> Unit)? = null,
    onReactionClick: ((Reaction, Comment) -> Unit)? = null,
    onDeleteClick: ((Comment) -> Unit)? = null,
) {
    LazyColumn(
        verticalArrangement = spacedBy(16.dp),
        overscrollEffect = null,
        modifier = modifier,
    ) {
        state.comment?.let { comment ->
            item {
                val isUserComment = state.user?.ids?.trakt == comment.user.ids.trakt
                CommentContent(
                    user = state.user,
                    comment = comment,
                    commentReplies = state.replies,
                    listReactions = state.reactions,
                    userReactions = (state.userReactions ?: emptyMap()).toImmutableMap(),
                    reactionsEnabled = state.user != null && !isUserComment,
                    onReplyLoaded = onReplyLoaded,
                    onReactionClick = onReactionClick,
                    onDeleteClick = { onDeleteClick?.invoke(comment) },
                )
            }
        }

        when {
            state.loading.isLoading &&
                (state.comment?.replies ?: 0) > 0 -> {
                item {
                    CommentSkeletonCard(
                        containerColor = TraktTheme.colors.commentReplyContainer,
                        shimmerColor = Shade800,
                        modifier = Modifier
                            .height(132.dp)
                            .fillMaxWidth(),
                    )
                }
            }
            state.loading == DONE && state.replies.isNullOrEmpty() -> {
                item {
                    Text(
                        text = stringResource(R.string.list_placeholder_replies),
                        color = TraktTheme.colors.textPrimary,
                        style = TraktTheme.typography.heading6,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .height(24.dp)
                            .wrapContentHeight()
                            .fillMaxWidth(),
                    )
                }
            }
        }

        item {
            Spacer(
                modifier = Modifier
                    .height(64.dp),
            )
        }
    }
}

@Composable
private fun CommentContent(
    user: User?,
    comment: Comment,
    commentReplies: ImmutableList<Comment>?,
    listReactions: ImmutableMap<Int, ReactionsSummary>?,
    reactionsEnabled: Boolean,
    userReactions: ImmutableMap<Int, Reaction?>,
    onReplyLoaded: ((Comment) -> Unit)? = null,
    onReactionClick: ((Reaction, Comment) -> Unit)? = null,
    onDeleteClick: (() -> Unit)? = null,
) {
    var isCollapsed by remember { mutableStateOf(true) }

    Column(
        verticalArrangement = spacedBy(0.dp),
    ) {
        CommentHeader(
            comment = comment,
            deleteEnabled = user?.ids?.trakt == comment.user.ids.trakt,
            onDeleteClick = onDeleteClick,
            modifier = Modifier.padding(top = 5.dp),
        )

        Text(
            text = comment.commentNoSpoilers,
            style = TraktTheme.typography.paragraphSmall.copy(lineHeight = 1.3.em),
            color = TraktTheme.colors.textSecondary,
            overflow = if (isCollapsed) TextOverflow.Ellipsis else TextOverflow.Clip,
            maxLines = if (isCollapsed) 20 else Int.MAX_VALUE,
            modifier = Modifier
                .onClick {
                    isCollapsed = !isCollapsed
                }
                .padding(top = 16.dp),
        )

        CommentFooter(
            reactions = listReactions?.get(comment.id),
            reactionsEnabled = reactionsEnabled,
            userReaction = userReactions[comment.id],
            onReactionClick = { onReactionClick?.invoke(it, comment) },
            modifier = Modifier
                .padding(top = 16.dp),
        )

        Crossfade(
            targetState = commentReplies,
            animationSpec = tween(200),
        ) { replies ->
            if (!replies.isNullOrEmpty()) {
                Column(
                    verticalArrangement = spacedBy(16.dp),
                    modifier = Modifier
                        .padding(top = 20.dp),
                ) {
                    for (reply in replies) {
                        CommentReplyCard(
                            comment = reply,
                            reactions = listReactions?.get(reply.id),
                            userReaction = userReactions[reply.id],
                            onRequestReactions = { onReplyLoaded?.invoke(reply) },
                            reactionsEnabled = reactionsEnabled,
                            onReactionClick = { onReactionClick?.invoke(it, reply) },
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CommentHeader(
    comment: Comment,
    deleteEnabled: Boolean,
    modifier: Modifier = Modifier,
    onDeleteClick: (() -> Unit)? = null,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Absolute.spacedBy(12.dp),
        modifier = modifier,
    ) {
        Box(
            modifier = Modifier.size(36.dp),
        ) {
            val avatarBorder = when {
                comment.user.isAnyVip -> TraktTheme.colors.vipAccent
                else -> Color.Transparent
            }
            val avatar = comment.user.images?.avatar?.full
            if (avatar != null) {
                AsyncImage(
                    model = avatar,
                    contentDescription = "User avatar",
                    contentScale = ContentScale.Crop,
                    error = painterResource(R.drawable.ic_person_placeholder),
                    modifier = Modifier
                        .border(2.dp, avatarBorder, CircleShape)
                        .clip(CircleShape),
                )
            } else {
                Image(
                    painter = painterResource(R.drawable.ic_person_placeholder),
                    contentDescription = null,
                    modifier = Modifier
                        .border(2.dp, avatarBorder, CircleShape)
                        .clip(CircleShape),
                )
            }

//            comment.userLiteRating?.let {
//                Icon(
//                    painter = painterResource(it.iconRes),
//                    contentDescription = it.name,
//                    tint = it.tint,
//                    modifier = Modifier
//                        .align(Alignment.TopEnd)
//                        .graphicsLayer {
//                            translationX = 4.dp.toPx()
//                            translationY = -4.dp.toPx()
//                        }
//                        .background(Shade500, shape = CircleShape)
//                        .size(18.dp)
//                        .padding(3.dp),
//                )
//            }
        }

        Column(verticalArrangement = Arrangement.Absolute.spacedBy(2.dp)) {
            Row(
                horizontalArrangement = Arrangement.Absolute.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = comment.user.displayName,
                    style = TraktTheme.typography.paragraph.copy(fontWeight = FontWeight.W600),
                    color = TraktTheme.colors.textPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Text(
                text = comment.createdAt.format(longDateTimeFormat),
                style = TraktTheme.typography.meta,
                color = TraktTheme.colors.textSecondary
                    .copy(alpha = 0.66f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }

        if (deleteEnabled) {
            Spacer(modifier = Modifier.weight(1f))
            Icon(
                painter = painterResource(R.drawable.ic_trash),
                contentDescription = null,
                tint = TraktTheme.colors.textPrimary,
                modifier = Modifier
                    .align(Alignment.Top)
                    .size(20.dp)
                    .graphicsLayer {
                        translationY = (-1).dp.toPx()
                    }
                    .onClick {
                        onDeleteClick?.invoke()
                    },
            )
        }
    }
}

@Composable
private fun CommentFooter(
    reactions: ReactionsSummary?,
    reactionsEnabled: Boolean,
    userReaction: Reaction?,
    modifier: Modifier = Modifier,
    onReactionClick: ((Reaction) -> Unit)? = null,
) {
    val scope = rememberCoroutineScope()
    val tooltipState = rememberTooltipState(isPersistent = true)

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 2.dp),
    ) {
        ReactionsToolTip(
            state = tooltipState,
            reactions = reactions,
            userReaction = userReaction,
            onReactionClick = onReactionClick,
        ) {
            ReactionsSummaryChip(
                reactions = reactions,
                userReaction = userReaction,
                enabled = reactionsEnabled,
                modifier = Modifier.onClick {
                    if (reactions == null || !reactionsEnabled) {
                        return@onClick
                    }
                    scope.launch {
                        if (tooltipState.isVisible) {
                            tooltipState.dismiss()
                        } else {
                            tooltipState.show()
                        }
                    }
                },
            )
        }
    }
}

@OptIn(ExperimentalCoilApi::class)
@Preview(
    showBackground = true,
    backgroundColor = 0xFF212427,
)
@Composable
private fun Preview() {
    TraktTheme {
        val previewHandler = AsyncImagePreviewHandler {
            ColorImage(Color.Blue.toArgb())
        }
        CompositionLocalProvider(LocalAsyncImagePreviewHandler provides previewHandler) {
            Column(
                verticalArrangement = spacedBy(48.dp),
            ) {
                CommentDetailsViewContent(
                    state = CommentDetailsState(
                        comment = PreviewData.comment1,
                        loading = LoadingState.LOADING,
                    ),
                )

                CommentDetailsViewContent(
                    state = CommentDetailsState(
                        user = PreviewData.user1,
                        comment = PreviewData.comment1,
                        replies = listOf(
                            PreviewData.comment1,
                        ).toImmutableList(),
                        loading = DONE,
                    ),
                )
            }
        }
    }
}
