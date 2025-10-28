@file:OptIn(ExperimentalMaterial3Api::class)

package tv.trakt.trakt.core.comments.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Arrangement.Absolute.spacedBy
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.font.FontWeight.Companion.W700
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import coil3.ColorImage
import coil3.annotation.ExperimentalCoilApi
import coil3.compose.AsyncImage
import coil3.compose.AsyncImagePreviewHandler
import coil3.compose.LocalAsyncImagePreviewHandler
import kotlinx.coroutines.launch
import tv.trakt.trakt.common.Config.webUserUrl
import tv.trakt.trakt.common.helpers.extensions.longDateTimeFormat
import tv.trakt.trakt.common.helpers.extensions.onClick
import tv.trakt.trakt.common.helpers.preview.PreviewData
import tv.trakt.trakt.common.model.Comment
import tv.trakt.trakt.common.model.User
import tv.trakt.trakt.common.model.reactions.Reaction
import tv.trakt.trakt.common.model.reactions.ReactionsSummary
import tv.trakt.trakt.common.ui.theme.colors.Shade500
import tv.trakt.trakt.core.reactions.ui.ReactionsSummaryChip
import tv.trakt.trakt.core.reactions.ui.ReactionsToolTip
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.theme.HorizontalImageAspectRatio
import tv.trakt.trakt.ui.theme.TraktTheme

@Composable
internal fun CommentCard(
    comment: Comment,
    modifier: Modifier = Modifier,
    reactions: ReactionsSummary? = null,
    reactionsEnabled: Boolean = true,
    userReaction: Reaction? = null,
    maxLines: Int = 4,
    corner: Dp = 24.dp,
    onClick: () -> Unit,
    onRequestReactions: (() -> Unit)? = null,
    onReactionClick: ((Reaction) -> Unit)? = null,
) {
    val uriHandler = LocalUriHandler.current

    LaunchedEffect(comment.id) {
        if (reactions == null) {
            onRequestReactions?.invoke()
        }
    }

    Card(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(corner),
        colors = cardColors(
            containerColor = TraktTheme.colors.commentContainer,
        ),
        content = {
            CommentCardContent(
                comment = comment,
                maxLines = maxLines,
                reactions = reactions,
                reactionsEnabled = reactionsEnabled,
                userReaction = userReaction,
                onReactionClick = onReactionClick,
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
    comment: Comment,
    reactions: ReactionsSummary?,
    reactionsEnabled: Boolean,
    userReaction: Reaction?,
    maxLines: Int,
    modifier: Modifier = Modifier,
    onUserClick: ((User) -> Unit)? = null,
    onReactionClick: ((Reaction) -> Unit)? = null,
) {
    Column(
        verticalArrangement = spacedBy(0.dp),
        modifier = modifier
            .padding(vertical = 16.dp)
            .fillMaxSize(),
    ) {
        CommentHeader(
            comment = comment,
            onUserClick = onUserClick,
            modifier = Modifier.padding(horizontal = 16.dp),
        )

        Text(
            text = comment.commentNoSpoilers,
            style = TraktTheme.typography.paragraphSmall.copy(lineHeight = 1.3.em),
            color = TraktTheme.colors.textSecondary,
            maxLines = maxLines,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.then(
                if (comment.hasSpoilers) {
                    Modifier
                        .blur(4.dp)
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                } else {
                    Modifier
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                },
            ),
        )

        Spacer(modifier = Modifier.weight(1f))

        CommentFooter(
            comment = comment,
            reactions = reactions,
            reactionsEnabled = reactionsEnabled,
            userReaction = userReaction,
            onReactionClick = onReactionClick,
            modifier = Modifier
                .padding(
                    start = 16.dp,
                    end = 20.dp,
                ),
        )
    }
}

@Composable
private fun CommentHeader(
    comment: Comment,
    modifier: Modifier = Modifier,
    onUserClick: ((User) -> Unit)? = null,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = spacedBy(12.dp),
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

            comment.userLiteRating?.let {
                Icon(
                    painter = painterResource(it.iconRes),
                    contentDescription = it.name,
                    tint = it.tint,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .graphicsLayer {
                            translationX = 4.dp.toPx()
                            translationY = -4.dp.toPx()
                        }
                        .background(Shade500, shape = CircleShape)
                        .size(18.dp)
                        .padding(3.dp),
                )
            }
        }

        Column(verticalArrangement = spacedBy(2.dp)) {
            Row(
                horizontalArrangement = spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = stringResource(
                        if (comment.isReview) {
                            R.string.text_review_by
                        } else {
                            R.string.text_shout_by
                        },
                    ),
                    style = TraktTheme.typography.paragraph,
                    color = TraktTheme.colors.textSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
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
    }
}

@Composable
private fun CommentFooter(
    comment: Comment,
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

        Row(
            horizontalArrangement = spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            val tint = when (comment.replies) {
                0 -> TraktTheme.colors.textSecondary
                else -> TraktTheme.colors.textPrimary
            }
            Icon(
                painter = painterResource(R.drawable.ic_comment),
                contentDescription = "Replies",
                tint = tint,
                modifier = Modifier.size(18.dp),
            )
            Text(
                text = "${comment.replies}",
                style = TraktTheme.typography.paragraphSmall.copy(fontWeight = W700),
                color = tint,
            )
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
                    comment = PreviewData.comment1.copy(userRating = 1, comment = "Lorem Ipsum"),
                    modifier = Modifier
                        .height(TraktTheme.size.commentCardSize)
                        .aspectRatio(HorizontalImageAspectRatio),
                )
                CommentCard(
                    onClick = {},
                    comment = PreviewData.comment1.copy(userRating = 10),
                    modifier = Modifier
                        .height(TraktTheme.size.commentCardSize)
                        .aspectRatio(HorizontalImageAspectRatio),
                )

                CommentCard(
                    onClick = {},
                    comment = PreviewData.comment1.copy(userRating = 7, replies = 0),
                    modifier = Modifier
                        .height(TraktTheme.size.commentCardSize)
                        .aspectRatio(HorizontalImageAspectRatio),
                )
            }
        }
    }
}
