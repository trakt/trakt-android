package tv.trakt.trakt.core.comments.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement.Absolute.spacedBy
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults.cardColors
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.font.FontWeight.Companion.W700
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import coil3.ColorImage
import coil3.annotation.ExperimentalCoilApi
import coil3.compose.AsyncImage
import coil3.compose.AsyncImagePreviewHandler
import coil3.compose.LocalAsyncImagePreviewHandler
import tv.trakt.trakt.common.helpers.extensions.highlightMentions
import tv.trakt.trakt.common.helpers.extensions.longDateTimeFormat
import tv.trakt.trakt.common.helpers.extensions.onClick
import tv.trakt.trakt.common.helpers.preview.PreviewData
import tv.trakt.trakt.common.model.Comment
import tv.trakt.trakt.common.ui.theme.colors.Shade500
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.theme.TraktTheme

@Composable
internal fun CommentReplyCard(
    comment: Comment,
    modifier: Modifier = Modifier,
    maxLines: Int = 10,
    onClick: (() -> Unit)? = null,
) {
    Card(
        onClick = onClick ?: {},
        modifier = modifier,
        shape = RoundedCornerShape(22.dp),
        colors = cardColors(
            containerColor = TraktTheme.colors.commentReplyContainer,
        ),
        content = {
            CommentReplyCardContent(
                comment = comment,
                maxLines = maxLines,
            )
        },
    )
}

@Composable
private fun CommentReplyCardContent(
    comment: Comment,
    maxLines: Int,
    modifier: Modifier = Modifier,
) {
    var isCollapsed by remember { mutableStateOf(true) }
    var showSpoilers by remember { mutableStateOf(false) }

    Column(
        verticalArrangement = spacedBy(0.dp),
        modifier = modifier
            .padding(vertical = 16.dp),
    ) {
        CommentHeader(
            comment = comment,
            modifier = Modifier.padding(horizontal = 16.dp),
        )

        val mentionsColor = TraktTheme.colors.textPrimary
        val mentionsText = remember(comment) {
            comment.commentNoSpoilers.highlightMentions(mentionsColor)
        }

        Text(
            text = mentionsText,
            style = TraktTheme.typography.paragraphSmall.copy(lineHeight = 1.3.em),
            color = TraktTheme.colors.textSecondary,
            overflow = if (isCollapsed) TextOverflow.Ellipsis else TextOverflow.Clip,
            maxLines = if (isCollapsed) maxLines else Int.MAX_VALUE,
            modifier = Modifier
                .onClick {
                    if (comment.hasSpoilers && !showSpoilers) {
                        showSpoilers = true
                    } else {
                        isCollapsed = !isCollapsed
                    }
                }
                .then(
                    if (comment.hasSpoilers && !showSpoilers) {
                        Modifier
                            .blur(4.dp)
                            .padding(top = 12.dp)
                            .padding(horizontal = 16.dp)
                    } else {
                        Modifier
                            .padding(top = 12.dp)
                            .padding(horizontal = 16.dp)
                    },
                ),
        )
    }
}

@Composable
private fun CommentHeader(
    comment: Comment,
    modifier: Modifier = Modifier,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = spacedBy(12.dp),
        modifier = modifier,
    ) {
        Box(
            modifier = Modifier.size(36.dp),
        ) {
            val avatarBorder = when {
                comment.user.isAnyVip -> Color.Red
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
                    text = stringResource(R.string.text_reply_by),
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
    modifier: Modifier = Modifier,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = spacedBy(16.dp, Alignment.End),
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 2.dp),
    ) {
//        Row(
//            horizontalArrangement = spacedBy(3.dp),
//            verticalAlignment = Alignment.CenterVertically,
//        ) {
//            Icon(
//                painter = painterResource(R.drawable.ic_thumb_up),
//                contentDescription = "Likes",
//                tint = TraktTheme.colors.textSecondary,
//                modifier = Modifier.size(16.dp),
//            )
//            Text(
//                text = stringResource(R.string.button_text_comment_likes, comment.likes).uppercase(),
//                style = TraktTheme.typography.paragraphSmall.copy(fontWeight = W700),
//                color = TraktTheme.colors.textSecondary,
//            )
//        }

        Row(
            horizontalArrangement = spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically,
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
}

@OptIn(ExperimentalCoilApi::class)
@Preview
@Composable
private fun Preview() {
    TraktTheme {
        val previewHandler = AsyncImagePreviewHandler {
            ColorImage(Color.LightGray.toArgb())
        }
        CompositionLocalProvider(LocalAsyncImagePreviewHandler provides previewHandler) {
            Column(
                verticalArrangement = spacedBy(16.dp),
            ) {
                CommentReplyCard(
                    onClick = {},
                    comment = PreviewData.comment1
                        .copy(userRating = 1, comment = "Lorem Ipsum with @johnlegend"),
                )
                CommentReplyCard(
                    onClick = {},
                    comment = PreviewData.comment1.copy(userRating = 10),
                )

                CommentReplyCard(
                    onClick = {},
                    comment = PreviewData.comment1.copy(userRating = 7),
                )
            }
        }
    }
}
