package tv.trakt.trakt.core.today.stories.ui

import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.BlurredEdgeTreatment
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import tv.trakt.trakt.common.helpers.extensions.onClick
import tv.trakt.trakt.common.ui.theme.colors.Red500
import tv.trakt.trakt.core.today.model.FollowingActivity.Detail
import tv.trakt.trakt.helpers.extensions.TraktThemeLightDark
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.theme.TraktTheme

private const val MAX_LINES = 4

private val GifHeight = 120.dp
private val GifOnlyPadding = 10.dp
private val BubbleElevation = 2.dp
private val SpoilerBlur = 6.dp

private val BubbleShape = RoundedCornerShape(16.dp)

@Composable
internal fun TodayCommentBubble(
    comment: Detail.Comment,
    modifier: Modifier = Modifier,
) {
    var isRevealed by remember(comment.id) { mutableStateOf(false) }

    TodayCommentBubbleContent(
        comment = comment,
        isSpoilerHidden = comment.isSpoiler && !isRevealed,
        onReveal = { isRevealed = true },
        modifier = modifier,
    )
}

@Composable
private fun TodayCommentBubbleContent(
    comment: Detail.Comment,
    isSpoilerHidden: Boolean,
    modifier: Modifier = Modifier,
    onReveal: () -> Unit,
) {
    val hasText = comment.text.isNotBlank()
    val isGifOnly = !hasText && comment.gifUrl != null && !isSpoilerHidden

    // Blur needs Android 12. Older versions keep the content out until revealed.
    val isBlurSupported = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
    val showContent = !isSpoilerHidden || isBlurSupported
    // Unbounded so the blur spreads past the text bounds instead of ending in a hard edge.
    val textBlur = if (isSpoilerHidden) Modifier.blur(SpoilerBlur, BlurredEdgeTreatment.Unbounded) else Modifier
    val gifBlur = if (isSpoilerHidden) Modifier.blur(SpoilerBlur) else Modifier

    Column(
        verticalArrangement = spacedBy(10.dp),
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation = BubbleElevation,
                shape = BubbleShape,
                ambientColor = Color.Black,
                spotColor = Color.Black,
            )
            .background(TraktTheme.colors.commentContainer, BubbleShape)
            .clip(BubbleShape)
            .onClick(
                enabled = isSpoilerHidden,
                onClick = onReveal,
            )
            .padding(
                horizontal = if (isGifOnly) GifOnlyPadding else 16.dp,
                vertical = if (isGifOnly) GifOnlyPadding else 12.dp,
            ),
    ) {
        if (isSpoilerHidden) {
            Text(
                text = stringResource(R.string.text_spoiler).uppercase(),
                style = TraktTheme.typography.meta,
                color = Red500,
            )
        }

        if (!showContent) return@Column

        if (hasText) {
            Text(
                text = comment.text.trim(),
                style = TraktTheme.typography.paragraphSmall,
                color = TraktTheme.colors.textPrimary,
                maxLines = MAX_LINES,
                overflow = TextOverflow.Ellipsis,
                modifier = textBlur,
            )
        }

        comment.gifUrl?.let {
            AsyncImage(
                model = it,
                contentDescription = null,
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .height(GifHeight)
                    .clip(RoundedCornerShape(8.dp))
                    .then(gifBlur),
            )
        }
    }
}

private val previewComment = Detail.Comment(
    id = 1,
    text = "That finale was something else. Did not see the twist coming at all.",
    gifUrl = null,
    isSpoiler = false,
    isReview = false,
    likeCount = 0,
    replyCount = 0,
)

private const val PREVIEW_GIF_URL = "https://media.giphy.com/media/l0MYt5jPR6QX5pnqM/giphy.gif"

@Composable
private fun PreviewColumn(content: @Composable () -> Unit) {
    TraktThemeLightDark {
        Column(
            verticalArrangement = spacedBy(12.dp),
            modifier = Modifier.padding(16.dp),
        ) {
            content()
        }
    }
}

@Preview(
    showBackground = true,
    backgroundColor = 0xFF131517,
    widthDp = 360,
)
@Composable
private fun Preview() {
    PreviewColumn {
        TodayCommentBubble(comment = previewComment)
        TodayCommentBubble(comment = previewComment.copy(isSpoiler = true))
    }
}

@Preview(
    showBackground = true,
    backgroundColor = 0xFF131517,
    widthDp = 360,
)
@Composable
private fun LengthPreview() {
    PreviewColumn {
        TodayCommentBubble(comment = previewComment.copy(text = "Wow."))
        TodayCommentBubble(
            comment = previewComment.copy(
                text = "Every episode this season has been building to this and it paid off. " +
                    "The writing, the score, the last ten minutes in particular. I had to sit " +
                    "in silence for a while after the credits rolled. Easily one of the best " +
                    "hours of television this year, and I will be thinking about it for weeks.",
            ),
        )
    }
}

@Preview(
    showBackground = true,
    backgroundColor = 0xFF131517,
    widthDp = 360,
)
@Composable
private fun GifPreview() {
    PreviewColumn {
        TodayCommentBubble(
            comment = previewComment.copy(gifUrl = PREVIEW_GIF_URL),
        )
        TodayCommentBubble(
            comment = previewComment.copy(text = "", gifUrl = PREVIEW_GIF_URL),
        )
        TodayCommentBubble(
            comment = previewComment.copy(isSpoiler = true, gifUrl = PREVIEW_GIF_URL),
        )
    }
}

@Preview(
    showBackground = true,
    backgroundColor = 0xFF131517,
    widthDp = 360,
)
@Composable
private fun SpoilerPreview() {
    val spoiler = previewComment.copy(isSpoiler = true)

    PreviewColumn {
        TodayCommentBubbleContent(comment = spoiler, isSpoilerHidden = true, onReveal = {})
        TodayCommentBubbleContent(comment = spoiler, isSpoilerHidden = false, onReveal = {})
        TodayCommentBubbleContent(
            comment = spoiler.copy(text = "", gifUrl = PREVIEW_GIF_URL),
            isSpoilerHidden = true,
            onReveal = {},
        )
        TodayCommentBubbleContent(
            comment = spoiler.copy(text = "", gifUrl = PREVIEW_GIF_URL),
            isSpoilerHidden = false,
            onReveal = {},
        )
    }
}

@Preview(
    showBackground = true,
    backgroundColor = 0xFF131517,
    widthDp = 360,
)
@Composable
private fun ReviewPreview() {
    PreviewColumn {
        TodayCommentBubble(
            comment = previewComment.copy(
                isReview = true,
                likeCount = 42,
                replyCount = 7,
                text = "A slow first act gives way to one of the most confident endings the " +
                    "genre has seen in years. The cast carries the quieter scenes well.",
            ),
        )
    }
}

@Preview(
    showBackground = true,
    backgroundColor = 0xFF131517,
    widthDp = 360,
    fontScale = 1.5F,
)
@Composable
private fun LargeFontPreview() {
    PreviewColumn {
        TodayCommentBubble(comment = previewComment)
        TodayCommentBubble(comment = previewComment.copy(isSpoiler = true))
    }
}

@Preview(
    showBackground = true,
    backgroundColor = 0xFF131517,
    widthDp = 360,
    locale = "ar-rSA",
)
@Composable
private fun RtlPreview() {
    PreviewColumn {
        TodayCommentBubble(
            comment = previewComment.copy(text = "كانت الحلقة الأخيرة رائعة حقاً. لم أتوقع هذه النهاية."),
        )
        TodayCommentBubble(comment = previewComment.copy(isSpoiler = true))
    }
}
