package tv.trakt.trakt.app.core.streamings.views

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement.SpaceBetween
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.tv.material3.Border
import androidx.tv.material3.Button
import androidx.tv.material3.ButtonDefaults
import androidx.tv.material3.Text
import tv.trakt.trakt.app.ui.theme.TraktTheme
import tv.trakt.trakt.common.model.streamings.YouTubeSpecial
import tv.trakt.trakt.resources.R

private val YouTubeRed = Color(0xFFFF0000)

@Composable
internal fun AllStreamingYouTubeItemView(
    special: YouTubeSpecial,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
) {
    val shape = RoundedCornerShape(12.dp)
    val containerColor = Color.White
    val contentColor = Color.Black

    val focusedBorder = Border(
        border = BorderStroke(
            width = 3.dp,
            color = TraktTheme.colors.accent,
        ),
        shape = shape,
    )

    Button(
        contentPadding = PaddingValues(horizontal = 10.dp),
        modifier = modifier.height(45.dp),
        shape = ButtonDefaults.shape(
            shape = shape,
            focusedDisabledShape = shape,
        ),
        border = ButtonDefaults.border(
            focusedBorder = focusedBorder,
            disabledBorder = Border.None,
            focusedDisabledBorder = focusedBorder,
        ),
        colors = ButtonDefaults.colors(
            containerColor = containerColor,
            contentColor = contentColor,
            focusedContainerColor = containerColor,
            focusedContentColor = contentColor,
            pressedContainerColor = containerColor,
            pressedContentColor = contentColor,
        ),
        scale = ButtonDefaults.scale(
            focusedScale = 1.04f,
        ),
        onClick = onClick,
    ) {
        Row(
            verticalAlignment = CenterVertically,
            horizontalArrangement = SpaceBetween,
            modifier = Modifier.fillMaxSize(),
        ) {
            Image(
                painter = painterResource(R.drawable.ic_youtube_full),
                contentDescription = null,
                contentScale = ContentScale.FillHeight,
                colorFilter = ColorFilter.tint(YouTubeRed),
                modifier = Modifier.height(14.dp),
            )

            Text(
                text = stringResource(
                    when {
                        special.isOfficial -> R.string.list_title_streaming_free
                        else -> R.string.tag_label_fan_upload
                    },
                ).uppercase(),
                color = contentColor,
                style = TraktTheme.typography.buttonTertiary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .padding(start = 8.dp)
                    .border(1.2.dp, contentColor, RoundedCornerShape(4.dp))
                    .padding(horizontal = 6.dp, vertical = 4.dp),
            )
        }
    }
}

@Preview(widthDp = 300)
@Composable
private fun Preview() {
    TraktTheme {
        AllStreamingYouTubeItemView(
            special = YouTubeSpecial(videoId = "abc", isOfficial = true),
        )
    }
}

@Preview(widthDp = 300)
@Composable
private fun Preview2() {
    TraktTheme {
        AllStreamingYouTubeItemView(
            special = YouTubeSpecial(videoId = "abc", isOfficial = false),
        )
    }
}
