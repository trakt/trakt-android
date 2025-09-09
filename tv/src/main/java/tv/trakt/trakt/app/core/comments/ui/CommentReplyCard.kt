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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
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
import coil3.annotation.ExperimentalCoilApi
import coil3.compose.AsyncImage
import tv.trakt.trakt.app.common.model.Comment
import tv.trakt.trakt.app.common.ui.TvVipChip
import tv.trakt.trakt.app.helpers.preview.PreviewData
import tv.trakt.trakt.app.ui.theme.TraktTheme
import tv.trakt.trakt.common.helpers.extensions.longDateTimeFormat
import tv.trakt.trakt.common.ui.theme.colors.Shade500
import tv.trakt.trakt.resources.R

@Composable
internal fun CommentReplyCard(
    comment: Comment,
    modifier: Modifier = Modifier,
) {
    var spoilersHidden by remember { mutableStateOf(true) }
    var isExpanded by remember { mutableStateOf(false) }

    Card(
        onClick = {
            spoilersHidden = !spoilersHidden
            isExpanded = !isExpanded
        },
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
            containerColor = TraktTheme.colors.commentReplyContainer,
            focusedContainerColor = TraktTheme.colors.commentReplyContainer,
        ),
        scale = CardDefaults.scale(focusedScale = 1.02f),
        content = {
            CommentCardContent(
                comment = comment,
                spoilersHidden = spoilersHidden,
                isExpanded = isExpanded,
            )
        },
    )
}

@Composable
private fun CommentCardContent(
    comment: Comment,
    spoilersHidden: Boolean,
    isExpanded: Boolean,
) {
    Column(
        verticalArrangement = spacedBy(0.dp, Alignment.Top),
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
    ) {
        CommentHeader(comment)

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = comment.commentNoSpoilers,
            style = TraktTheme.typography.paragraphSmall,
            color = TraktTheme.colors.textSecondary,
            maxLines = if (isExpanded) Int.MAX_VALUE else 3,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.then(
                if (comment.hasSpoilers && spoilersHidden) {
                    Modifier.blur(4.dp)
                } else {
                    Modifier
                },
            ),
        )

        Spacer(modifier = Modifier.height(16.dp))

        CommentFooter(
            comment = comment,
        )
    }
}

@Composable
private fun CommentHeader(comment: Comment) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = spacedBy(8.dp),
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
private fun CommentFooter(comment: Comment) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = spacedBy(16.dp),
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
    }
}

@OptIn(ExperimentalCoilApi::class)
@Preview
@Composable
fun CommentReplyPreview() {
    TraktTheme {
        Column(
            verticalArrangement = spacedBy(32.dp),
        ) {
            CommentReplyCard(
                comment = PreviewData.comment1,
            )
        }
    }
}
