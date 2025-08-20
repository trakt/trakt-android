package tv.trakt.trakt.app.core.comments.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement.Absolute.spacedBy
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
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
import androidx.tv.material3.Border
import androidx.tv.material3.Card
import androidx.tv.material3.CardDefaults
import androidx.tv.material3.Icon
import androidx.tv.material3.Text
import coil3.ColorImage
import coil3.annotation.ExperimentalCoilApi
import coil3.compose.AsyncImage
import coil3.compose.AsyncImagePreviewHandler
import coil3.compose.LocalAsyncImagePreviewHandler
import tv.trakt.trakt.app.common.model.Comment
import tv.trakt.trakt.app.common.ui.TvVipChip
import tv.trakt.trakt.app.helpers.longDateTimeFormat
import tv.trakt.trakt.app.helpers.preview.PreviewData
import tv.trakt.trakt.app.ui.theme.TraktTheme
import tv.trakt.trakt.common.ui.theme.colors.Shade500
import tv.trakt.trakt.resources.R

@Composable
internal fun CommentCard(
    comment: Comment,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    Card(
        onClick = onClick,
        modifier = modifier,
        shape = CardDefaults.shape(
            shape = RoundedCornerShape(12.dp),
        ),
        border = CardDefaults.border(
            focusedBorder = Border(
                border = BorderStroke(
                    width = (2.75).dp,
                    color = TraktTheme.colors.accent,
                ),
                shape = RoundedCornerShape(12.dp),
            ),
        ),
        colors = CardDefaults.colors(
            containerColor = TraktTheme.colors.commentContainer,
            focusedContainerColor = TraktTheme.colors.commentContainer,
        ),
        scale = CardDefaults.scale(
            focusedScale = 1.02f,
        ),
        content = {
            CommentCardContent(comment)
        },
    )
}

@Composable
private fun CommentCardContent(comment: Comment) {
    Column(
        verticalArrangement = spacedBy(0.dp, Alignment.CenterVertically),
        modifier = Modifier
            .fillMaxSize()
            .padding(vertical = 16.dp),
    ) {
        CommentHeader(
            comment = comment,
            modifier = Modifier.padding(horizontal = 16.dp),
        )

        Text(
            text = comment.comment,
            style = TraktTheme.typography.paragraphSmall,
            color = TraktTheme.colors.textSecondary,
            maxLines = 3,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.then(
                if (comment.hasSpoilers) {
                    Modifier
                        .blur(4.dp)
                        .padding(16.dp)
                } else {
                    Modifier
                        .padding(16.dp)
                },
            ),
        )

        Spacer(modifier = Modifier.weight(1F))

        CommentFooter(
            comment = comment,
            modifier = Modifier
                .padding(horizontal = 16.dp),
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
        horizontalArrangement = spacedBy(8.dp),
        modifier = modifier,
    ) {
        Box(
            modifier = Modifier.size(36.dp),
        ) {
            val avatarBorder = if (comment.user.isAnyVip) Color.Red else Color.Transparent
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

        Column(verticalArrangement = spacedBy(3.dp)) {
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
                    text = comment.user.username,
                    style = TraktTheme.typography.paragraph.copy(fontWeight = FontWeight.W600),
                    color = TraktTheme.colors.textPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                if (comment.user.isAnyVip) {
                    TvVipChip()
                }
            }
            Text(
                text = comment.createdAt.format(longDateTimeFormat),
                style = TraktTheme.typography.paragraphSmall,
                color = TraktTheme.colors.textSecondary,
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
        horizontalArrangement = spacedBy(16.dp),
        modifier = modifier,
    ) {
        Row(
            horizontalArrangement = spacedBy(3.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_thumb_up),
                contentDescription = "Likes",
                tint = TraktTheme.colors.textSecondary,
                modifier = Modifier.size(16.dp),
            )
            Text(
                text = stringResource(R.string.button_text_comment_likes, comment.likes).uppercase(),
                style = TraktTheme.typography.paragraphSmall.copy(fontWeight = W700),
                color = TraktTheme.colors.textSecondary,
            )
        }

        Row(
            horizontalArrangement = spacedBy(2.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_comment),
                contentDescription = "Replies",
                tint = TraktTheme.colors.textSecondary,
                modifier = Modifier.size(18.dp),
            )
            Text(
                text = stringResource(R.string.button_text_comment_replies, comment.replies).uppercase(),
                style = TraktTheme.typography.paragraphSmall.copy(fontWeight = W700),
                color = TraktTheme.colors.textSecondary,
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
                verticalArrangement = spacedBy(32.dp),
            ) {
                CommentCard(
                    onClick = {},
                    comment = PreviewData.comment1.copy(userRating = 1),
                    modifier = Modifier
                        .height(TraktTheme.size.detailsCommentSize)
                        .aspectRatio(CardDefaults.HorizontalImageAspectRatio),
                )
                CommentCard(
                    onClick = {},
                    comment = PreviewData.comment1.copy(userRating = 10),
                    modifier = Modifier
                        .height(TraktTheme.size.detailsCommentSize)
                        .aspectRatio(CardDefaults.HorizontalImageAspectRatio),
                )

                CommentCard(
                    onClick = {},
                    comment = PreviewData.comment1.copy(userRating = 7),
                    modifier = Modifier
                        .height(TraktTheme.size.detailsCommentSize)
                        .aspectRatio(CardDefaults.HorizontalImageAspectRatio),
                )
            }
        }
    }
}
