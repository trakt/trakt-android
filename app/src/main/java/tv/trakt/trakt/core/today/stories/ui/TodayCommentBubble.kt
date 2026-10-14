package tv.trakt.trakt.core.today.stories.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import tv.trakt.trakt.core.today.model.FollowingActivity.Detail
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.theme.TraktTheme

private const val MAX_LINES = 4

private val GifHeight = 120.dp

// Square top-start corner points the bubble at the friend in the footer.
private val BubbleShape = RoundedCornerShape(
    topStart = 4.dp,
    topEnd = 16.dp,
    bottomEnd = 16.dp,
    bottomStart = 16.dp,
)

@Composable
internal fun TodayCommentBubble(
    comment: Detail.Comment,
    modifier: Modifier = Modifier,
) {
    Column(
        verticalArrangement = spacedBy(6.dp),
        modifier = modifier
            .fillMaxWidth()
            .background(TraktTheme.colors.commentContainer, BubbleShape)
            .padding(horizontal = 16.dp, vertical = 12.dp),
    ) {
        if (comment.isSpoiler) {
            Text(
                text = stringResource(R.string.text_spoiler).uppercase(),
                style = TraktTheme.typography.meta,
                color = TraktTheme.colors.accent,
            )
            return@Column
        }

        Text(
            text = comment.text,
            style = TraktTheme.typography.paragraphSmall,
            color = TraktTheme.colors.textPrimary,
            maxLines = MAX_LINES,
            overflow = TextOverflow.Ellipsis,
        )

        comment.gifUrl?.let {
            AsyncImage(
                model = it,
                contentDescription = null,
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .height(GifHeight)
                    .clip(RoundedCornerShape(8.dp)),
            )
        }
    }
}

@Preview
@Composable
private fun Preview() {
    TraktTheme {
        Column(
            verticalArrangement = spacedBy(12.dp),
            modifier = Modifier.padding(16.dp),
        ) {
            TodayCommentBubble(
                comment = Detail.Comment(
                    id = 1,
                    text = "That finale was something else. Did not see the twist coming at all.",
                    gifUrl = null,
                    isSpoiler = false,
                    isReview = false,
                    likeCount = 0,
                    replyCount = 0,
                ),
            )
            TodayCommentBubble(
                comment = Detail.Comment(
                    id = 2,
                    text = "",
                    gifUrl = null,
                    isSpoiler = true,
                    isReview = false,
                    likeCount = 0,
                    replyCount = 0,
                ),
            )
        }
    }
}
