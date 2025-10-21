package tv.trakt.trakt.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement.Absolute.spacedBy
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import tv.trakt.trakt.common.ui.theme.colors.Shade930
import tv.trakt.trakt.ui.theme.TraktTheme

@Composable
internal fun EpisodeProgressBar(
    modifier: Modifier = Modifier,
    startText: String? = null,
    endText: String? = null,
    textColor: Color = TraktTheme.colors.textPrimary,
    textStyle: TextStyle = TraktTheme.typography.meta,
    containerColor: Color = TraktTheme.colors.chipContainerOnContent,
    progress: Float = 0f,
    trackColor: Color = Shade930,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = spacedBy((2.5).dp),
        modifier = modifier
            .background(
                shape = RoundedCornerShape(100),
                color = containerColor,
            )
            .padding(
                horizontal = 9.dp,
                vertical = 4.dp,
            )
            .drawWithContent {
                if (progress > 0.1F) {
                    val cornerRadius = CornerRadius(size.width * 2)
                    val xOffset = (0.25).dp.toPx()
                    val yOffset = (5).dp.toPx()

                    drawRoundRect(
                        topLeft = Offset(xOffset, yOffset),
                        color = trackColor,
                        size = size.copy(
                            width = (size.width * progress) - (xOffset * 2),
                            height = size.height - (yOffset * 2),
                        ),
                        cornerRadius = cornerRadius,
                        style = Stroke(width = size.height),
                    )
                }

                drawContent()
            },
    ) {
        startText?.let {
            Text(
                text = startText,
                style = textStyle,
                color = textColor,
                maxLines = 1,
            )
        }

        Spacer(Modifier.weight(1F))

        endText?.let {
            Text(
                text = endText,
                style = textStyle,
                color = textColor,
                maxLines = 1,
            )
        }
    }
}

@Preview(widthDp = 200)
@Composable
private fun Preview1() {
    TraktTheme {
        EpisodeProgressBar(
            startText = "12 remaining",
            endText = "1h 23m",
        )
    }
}

@Preview(widthDp = 120)
@Composable
private fun Preview2() {
    TraktTheme {
        EpisodeProgressBar(
            startText = "12 remaining",
            endText = "1h 23m",
            progress = 0.75F,
        )
    }
}
