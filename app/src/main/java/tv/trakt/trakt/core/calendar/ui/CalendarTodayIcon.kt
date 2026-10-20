package tv.trakt.trakt.core.calendar.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import tv.trakt.trakt.common.helpers.extensions.nowLocalDay
import tv.trakt.trakt.common.helpers.extensions.onClick
import tv.trakt.trakt.ui.theme.TraktTheme

private val FadeDuration = tween<Float>(150)

private val IconSize = 22.dp
private val FrameSize = 19.dp
private val FrameStroke = 1.8.dp
private val FrameShape = RoundedCornerShape(4.dp)

// Sized in dp so system font scaling cannot push the number out of the frame.
private val DayNumberSize = 9.dp

@Composable
internal fun CalendarTodayIcon(
    visible: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
) {
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(FadeDuration),
        exit = fadeOut(FadeDuration),
        modifier = modifier,
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(IconSize)
                .onClick(onClick = onClick),
        ) {
            val fontSize = with(LocalDensity.current) { DayNumberSize.toSp() }
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(FrameSize)
                    .border(
                        width = FrameStroke,
                        color = TraktTheme.colors.textPrimary,
                        shape = FrameShape,
                    ),
            ) {
                Text(
                    text = nowLocalDay().dayOfMonth.toString(),
                    color = TraktTheme.colors.textPrimary,
                    style = TraktTheme.typography.meta.copy(
                        fontSize = fontSize,
                        lineHeight = fontSize,
                        letterSpacing = 0.em,
                        textAlign = TextAlign.Center,
                        lineHeightStyle = LineHeightStyle(
                            alignment = LineHeightStyle.Alignment.Center,
                            trim = LineHeightStyle.Trim.Both,
                        ),
                    ),
                    maxLines = 1,
                )
            }
        }
    }
}

@Preview
@Composable
private fun Preview() {
    TraktTheme {
        CalendarTodayIcon(visible = true)
    }
}
