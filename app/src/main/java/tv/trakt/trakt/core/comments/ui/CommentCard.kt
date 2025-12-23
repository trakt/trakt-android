@file:OptIn(ExperimentalMaterial3Api::class)

package tv.trakt.trakt.core.comments.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Arrangement.Absolute.spacedBy
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults.cardColors
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.font.FontWeight.Companion.W700
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
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
import tv.trakt.trakt.common.Config.webUserUrl
import tv.trakt.trakt.common.helpers.extensions.EmptyImmutableList
import tv.trakt.trakt.common.helpers.extensions.longDateTimeFormat
import tv.trakt.trakt.common.helpers.extensions.onClick
import tv.trakt.trakt.common.helpers.preview.PreviewData
import tv.trakt.trakt.common.model.Comment
import tv.trakt.trakt.common.model.User
import tv.trakt.trakt.common.model.reactions.Reaction
import tv.trakt.trakt.common.model.reactions.ReactionsSummary
import tv.trakt.trakt.common.ui.theme.colors.Purple400
import tv.trakt.trakt.core.reactions.ui.ReactionsSummaryChip
import tv.trakt.trakt.core.reactions.ui.ReactionsToolTip
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.theme.TraktTheme

private val EmptyReactionsSummary = emptyMap<Int, ReactionsSummary>().toImmutableMap()
private val EmptyReactions = emptyMap<Int, Reaction?>().toImmutableMap()

@Composable
internal fun CommentCard(
    user: User?,
    comment: Comment,
    modifier: Modifier = Modifier,
    replies: ImmutableList<Comment> = EmptyImmutableList,
    reactions: ImmutableMap<Int, ReactionsSummary> = EmptyReactionsSummary,
    userReactions: ImmutableMap<Int, Reaction?> = EmptyReactions,
    deleteEnabled: Boolean = true,
    replyEnabled: Boolean = false,
    repliesButtonEnabled: Boolean = false,
    repliesCountEnabled: Boolean = true,
    repliesLoading: Boolean = false,
    onClick: () -> Unit = {},
    onRequestReactions: ((Comment) -> Unit)? = null,
    onReactionClick: ((Reaction, Comment) -> Unit)? = null,
    onRepliesClick: (() -> Unit)? = null,
    onReplyClick: ((Comment) -> Unit)? = null,
    onReplyUserClick: ((Comment, User) -> Unit)? = null,
    onDeleteClick: (() -> Unit)? = null,
    onDeleteReplyClick: ((Comment) -> Unit)? = null,
) {
    val uriHandler = LocalUriHandler.current

    LaunchedEffect(comment.id) {
        if (reactions[comment.id] == null) {
            onRequestReactions?.invoke(comment)
        }
    }

    val isUserComment = remember(user) {
        comment.user.ids.trakt == user?.ids?.trakt
    }

    Card(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(24.dp),
        colors = cardColors(
            containerColor = TraktTheme.colors.commentContainer,
        ),
        border = when {
            isUserComment -> BorderStroke(2.dp, TraktTheme.colors.accent)
            else -> null
        },
        content = {
            CommentCardContent(
                user = user,
                comment = comment,
                replies = replies,
                reactions = reactions,
                userReactions = userReactions,
                deleteEnabled = deleteEnabled,
                replyEnabled = replyEnabled,
                repliesButtonEnabled = repliesButtonEnabled,
                repliesCountEnabled = repliesCountEnabled,
                repliesLoading = repliesLoading,
                onRequestReactions = onRequestReactions,
                onReactionClick = onReactionClick,
                onReplyClick = { onReplyClick?.invoke(comment) },
                onReplyUserClick = { onReplyUserClick?.invoke(comment, it) },
                onRepliesClick = onRepliesClick,
                onDeleteClick = onDeleteClick,
                onDeleteReplyClick = onDeleteReplyClick,
                onUserClick = {
                    uriHandler.openUri(
                        webUserUrl(it.username),
                    )
                },
            )
        },
    )
}

@Composable
private fun CommentCardContent(
    user: User?,
    comment: Comment,
    replies: ImmutableList<Comment>,
    reactions: ImmutableMap<Int, ReactionsSummary>,
    userReactions: ImmutableMap<Int, Reaction?>,
    deleteEnabled: Boolean,
    replyEnabled: Boolean,
    repliesButtonEnabled: Boolean,
    repliesCountEnabled: Boolean,
    repliesLoading: Boolean,
    modifier: Modifier = Modifier,
    onUserClick: ((User) -> Unit)? = null,
    onReactionClick: ((Reaction, Comment) -> Unit)? = null,
    onReplyClick: (() -> Unit)? = null,
    onReplyUserClick: ((User) -> Unit)? = null,
    onRepliesClick: (() -> Unit)? = null,
    onDeleteClick: (() -> Unit)? = null,
    onDeleteReplyClick: ((Comment) -> Unit)? = null,
    onRequestReactions: ((Comment) -> Unit)? = null,
) {
    var isSpoilerRevealed by remember { mutableStateOf(false) }

    val isUserComment = remember(user) {
        comment.user.ids.trakt == user?.ids?.trakt
    }

    Column(
        verticalArrangement = spacedBy(0.dp),
        modifier = modifier
            .padding(vertical = 16.dp)
            .fillMaxSize(),
    ) {
        CommentHeader(
            comment = comment,
            userComment = isUserComment,
            deleteEnabled = deleteEnabled,
            onUserClick = onUserClick,
            onDeleteClick = onDeleteClick,
            modifier = Modifier.padding(horizontal = 16.dp),
        )

        Text(
            text = comment.commentNoSpoilers,
            style = TraktTheme.typography.paragraphSmall.copy(lineHeight = 1.3.em),
            color = TraktTheme.colors.textSecondary,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.then(
                if (comment.hasSpoilers && !isUserComment && !isSpoilerRevealed) {
                    Modifier
                        .blur(4.dp)
                        .padding(horizontal = 16.dp)
                        .padding(top = 14.dp, bottom = 20.dp)
                        .onClick {
                            isSpoilerRevealed = true
                        }
                } else {
                    Modifier
                        .padding(horizontal = 16.dp)
                        .padding(top = 14.dp, bottom = 20.dp)
                },
            ),
        )

        Spacer(modifier = Modifier.weight(1f))

        CommentFooter(
            comment = comment,
            replies = replies,
            reactions = reactions[comment.id],
            reactionsEnabled = user != null && !isUserComment,
            repliesLoading = repliesLoading,
            repliesCountEnabled = repliesCountEnabled,
            repliesButtonEnabled = repliesButtonEnabled,
            userReaction = userReactions[comment.id],
            onReactionClick = onReactionClick,
            onReplyClick = onReplyClick,
            onRepliesClick = onRepliesClick,
            replyEnabled = replyEnabled,
            modifier = Modifier
                .padding(
                    start = 16.dp,
                    end = 20.dp,
                ),
        )

        if (replies.isNotEmpty()) {
            CommentRepliesContent(
                user = user,
                replies = replies,
                reactions = reactions,
                userReactions = userReactions,
                onReactionClick = onReactionClick,
                onReplyClick = { onReplyUserClick?.invoke(it.user) },
                onDeleteClick = onDeleteReplyClick,
                onRequestReactions = onRequestReactions,
            )
        }
    }
}

@Composable
private fun CommentRepliesContent(
    user: User?,
    replies: ImmutableList<Comment>,
    reactions: ImmutableMap<Int, ReactionsSummary>,
    userReactions: ImmutableMap<Int, Reaction?>,
    modifier: Modifier = Modifier,
    onReactionClick: ((Reaction, Comment) -> Unit)? = null,
    onRequestReactions: ((Comment) -> Unit)? = null,
    onReplyClick: ((Comment) -> Unit)? = null,
    onDeleteClick: ((Comment) -> Unit)? = null,
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = modifier
            .padding(top = 20.dp)
            .padding(horizontal = 16.dp),
    ) {
        for (reply in replies) {
            CommentReplyCard(
                user = user,
                reply = reply,
                reactions = reactions[reply.id],
                userReaction = userReactions[reply.id],
                onReactionClick = { onReactionClick?.invoke(it, reply) },
                onRequestReactions = { onRequestReactions?.invoke(reply) },
                onDeleteClick = { onDeleteClick?.invoke(reply) },
                onReplyClick = { onReplyClick?.invoke(reply) },
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Composable
private fun CommentHeader(
    comment: Comment,
    userComment: Boolean,
    deleteEnabled: Boolean,
    modifier: Modifier = Modifier,
    onUserClick: ((User) -> Unit)? = null,
    onDeleteClick: (() -> Unit)? = null,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = spacedBy(10.dp),
        modifier = modifier,
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .onClick {
                    onUserClick?.invoke(comment.user)
                },
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
        }

        Column(verticalArrangement = spacedBy(1.dp)) {
            Row(
                horizontalArrangement = spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = comment.user.displayName,
                    style = TraktTheme.typography.paragraph.copy(fontWeight = FontWeight.W600),
                    color = TraktTheme.colors.textPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.onClick {
                        onUserClick?.invoke(comment.user)
                    },
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

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = spacedBy(12.dp),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Spacer(modifier = Modifier.weight(1f))
            comment.user5Rating?.let { rating ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(3.dp),
                ) {
                    Text(
                        text = rating,
                        style = TraktTheme.typography.paragraphSmall.copy(fontWeight = W700),
                        color = TraktTheme.colors.textPrimary,
                        maxLines = 1,
                    )

                    Icon(
                        painter = painterResource(R.drawable.ic_star_trakt_on),
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier
                            .size(15.dp),
                    )
                }
            }

            if (userComment && deleteEnabled) {
                Icon(
                    painter = painterResource(R.drawable.ic_trash),
                    contentDescription = null,
                    tint = TraktTheme.colors.textPrimary,
                    modifier = Modifier
                        .size(20.dp)
                        .onClick {
                            onDeleteClick?.invoke()
                        },
                )
            }
        }
    }
}

@Composable
private fun CommentFooter(
    comment: Comment,
    replies: ImmutableList<Comment>,
    reactions: ReactionsSummary?,
    reactionsEnabled: Boolean,
    repliesCountEnabled: Boolean,
    repliesButtonEnabled: Boolean,
    replyEnabled: Boolean,
    repliesLoading: Boolean,
    userReaction: Reaction?,
    modifier: Modifier = Modifier,
    onReactionClick: ((Reaction, Comment) -> Unit)? = null,
    onReplyClick: (() -> Unit)? = null,
    onRepliesClick: (() -> Unit)? = null,
) {
    val scope = rememberCoroutineScope()
    val tooltipState = rememberTooltipState(isPersistent = true)

    Column(
        modifier = modifier,
        verticalArrangement = spacedBy(22.dp),
    ) {
        if (repliesButtonEnabled && comment.replies > 0) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(3.dp),
                modifier = Modifier.onClick(
                    enabled = !repliesLoading,
                ) {
                    onRepliesClick?.invoke()
                },
            ) {
                Icon(
                    painter = painterResource(
                        when {
                            replies.isNotEmpty() -> R.drawable.ic_collapsed
                            else -> R.drawable.ic_chevron_down_small
                        },
                    ),
                    contentDescription = "Replies",
                    tint = when {
                        repliesLoading -> TraktTheme.colors.textSecondary.copy(alpha = 0.5f)
                        else -> Purple400
                    },
                    modifier = Modifier
                        .size(16.dp)
                        .scale(
                            scaleX = when {
                                replies.isNotEmpty() -> 0.9F
                                else -> 1F
                            },
                            scaleY = 1F,
                        ),
                )
                Text(
                    text = stringResource(R.string.button_text_comment_replies, comment.replies),
                    style = TraktTheme.typography.meta.copy(fontSize = 12.sp),
                    color = when {
                        repliesLoading -> TraktTheme.colors.textSecondary.copy(alpha = 0.5f)
                        else -> Purple400
                    },
                )
            }
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 1.dp),
        ) {
            ReactionsToolTip(
                state = tooltipState,
                reactions = reactions,
                userReaction = userReaction,
                onReactionClick = { reaction ->
                    onReactionClick?.invoke(reaction, comment)
                },
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

            Row(
                horizontalArrangement = spacedBy(24.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (repliesCountEnabled && comment.replies > 0) {
                    Row(
                        horizontalArrangement = spacedBy(2.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.onClick(
                            enabled = !repliesLoading,
                        ) {
                            onRepliesClick?.invoke()
                        },
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_comment),
                            contentDescription = "Replies",
                            tint = TraktTheme.colors.textPrimary,
                            modifier = Modifier.size(18.dp),
                        )
                        Text(
                            text = "${comment.replies}",
                            style = TraktTheme.typography.paragraphSmall.copy(fontWeight = W700),
                            color = TraktTheme.colors.textPrimary,
                        )
                    }
                }

                if (replyEnabled) {
                    Icon(
                        painter = painterResource(R.drawable.ic_comment_plus),
                        contentDescription = null,
                        tint = TraktTheme.colors.textPrimary,
                        modifier = Modifier
                            .size(18.dp)
                            .onClick(enabled = !repliesLoading) {
                                onReplyClick?.invoke()
                            },
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalCoilApi::class)
@Preview
@Composable
fun CommentPreview() {
    TraktTheme {
        val previewHandler = AsyncImagePreviewHandler {
            ColorImage(Color.LightGray.toArgb())
        }
        CompositionLocalProvider(LocalAsyncImagePreviewHandler provides previewHandler) {
            Column(
                verticalArrangement = spacedBy(16.dp),
            ) {
                CommentCard(
                    onClick = {},
                    user = PreviewData.user1,
                    comment = PreviewData.comment1.copy(userRating = 1, comment = "Lorem Ipsum"),
                    replies = EmptyImmutableList,
                    modifier = Modifier
                        .height(TraktTheme.size.commentCardSize),
                )
                CommentCard(
                    onClick = {},
                    user = PreviewData.user1,
                    comment = PreviewData.comment1.copy(userRating = 10),
                    replies = listOf(PreviewData.comment1).toImmutableList(),
                    modifier = Modifier
                        .height(400.dp),
                )
            }
        }
    }
}
