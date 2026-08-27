package tv.trakt.trakt.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import tv.trakt.trakt.common.ui.theme.colors.Shade930
import tv.trakt.trakt.helpers.extensions.TraktThemeLightDark
import tv.trakt.trakt.ui.theme.TraktTheme

private const val MIN_VISIBLE_PROGRESS = 0.1F

private val BarShape = RoundedCornerShape(100)
private val TrackInset = 2.5.dp

@Composable
internal fun EpisodeProgressBar(
    modifier: Modifier = Modifier,
    startText: String? = null,
    endText: String? = null,
    textColor: Color = TraktTheme.colors.textPrimaryOnAccent,
    textStyle: TextStyle = TraktTheme.typography.meta,
    containerColor: Color = TraktTheme.colors.chipContainerOnContent,
    progress: Float = 0f,
    trackColor: Color = Shade930,
) {
    Box(
        modifier = modifier
            .heightIn(max = 22.dp)
            .clip(BarShape)
            .background(containerColor),
    ) {
        if (progress > MIN_VISIBLE_PROGRESS) {
            val filled = progress.coerceAtMost(1F)

            Row(
                Modifier
                    .matchParentSize()
                    .padding(TrackInset),
            ) {
                Box(
                    Modifier
                        .weight(filled)
                        .fillMaxHeight()
                        .background(color = trackColor, shape = BarShape),
                )

                if (filled < 1F) {
                    Spacer(Modifier.weight(1F - filled))
                }
            }
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = spacedBy(2.5.dp),
            modifier = Modifier.padding(
                horizontal = 9.dp,
                vertical = 4.dp,
            ),
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
                    overflow = TextOverflow.Ellipsis,
                    autoSize = TextAutoSize.StepBased(
                        maxFontSize = textStyle.fontSize,
                        minFontSize = 8.sp,
                        stepSize = 1.sp,
                    ),
                )
            }
        }
    }
}

@Preview(widthDp = 200)
@Composable
private fun Preview1() {
    TraktThemeLightDark {
        EpisodeProgressBar(
            startText = "12 remaining",
            endText = "1h 23m",
        )
    }
}

@Preview(widthDp = 200)
@Composable
private fun Preview2() {
    TraktThemeLightDark {
        EpisodeProgressBar(
            startText = "44m",
            endText = "12 remaining • 4h 34min",
            progress = 0.75F,
        )
    }
}

@Preview(widthDp = 200, locale = "ar")
@Composable
private fun PreviewRtl() {
    TraktThemeLightDark {
        EpisodeProgressBar(
            endText = "١٢ متبقية • ٤ س ٣٤ د",
            progress = 0.75F,
        )
    }
}
