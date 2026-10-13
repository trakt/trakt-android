package tv.trakt.trakt.core.main.ui.seasonal

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import tv.trakt.trakt.common.helpers.extensions.onClick
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.theme.TraktTheme
import kotlin.time.Duration.Companion.seconds

private val DurationMillis = 30.seconds.inWholeMilliseconds.toInt()

@Composable
internal fun SplashCloseButton(
    modifier: Modifier = Modifier,
    autoDismiss: Boolean = true,
    onClick: () -> Unit,
) {
    val currentOnClick by rememberUpdatedState(onClick)
    val progress = remember { Animatable(0F) }
    val color = TraktTheme.colors.textPrimary

    LaunchedEffect(autoDismiss) {
        if (!autoDismiss) return@LaunchedEffect
        progress.animateTo(
            targetValue = 1F,
            animationSpec = tween(
                durationMillis = DurationMillis,
                easing = LinearEasing,
            ),
        )
        currentOnClick()
    }

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .size(40.dp)
            .drawBehind {
                val stroke = 2.dp.toPx()
                drawArc(
                    color = color,
                    startAngle = -90F,
                    sweepAngle = 360F * progress.value,
                    useCenter = false,
                    topLeft = Offset(stroke / 2, stroke / 2),
                    size = Size(size.width - stroke, size.height - stroke),
                    style = Stroke(width = stroke, cap = StrokeCap.Round),
                )
            }
            .padding(4.dp)
            .clip(CircleShape)
            .border(1.dp, color, CircleShape)
            .onClick(onClick = onClick),
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_close),
            contentDescription = stringResource(R.string.button_label_close),
            tint = color,
            modifier = Modifier.size(16.dp),
        )
    }
}

@Preview
@Composable
private fun Preview() {
    TraktTheme {
        SplashCloseButton(
            autoDismiss = false,
            onClick = {},
            modifier = Modifier.padding(16.dp),
        )
    }
}
