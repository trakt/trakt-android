package tv.trakt.trakt.core.calendar.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import tv.trakt.trakt.common.helpers.extensions.onClick
import tv.trakt.trakt.core.calendar.model.CalendarView
import tv.trakt.trakt.ui.theme.TraktTheme
import kotlin.time.Duration.Companion.milliseconds

private const val ATTENTION_DELAY = 400L
private const val ATTENTION_POP_VELOCITY = 4F

@Composable
internal fun CalendarViewToggle(
    current: CalendarView,
    modifier: Modifier = Modifier,
    attention: Boolean = false,
    onViewClick: (CalendarView) -> Unit = {},
) {
    val target = current.toggle

    var attentionDone by rememberSaveable { mutableStateOf(false) }
    val scale = remember { Animatable(1F) }
    val rotation = remember { Animatable(0F) }
    val dotScale = remember { Animatable(if (attentionDone) 1F else 0F) }

    LaunchedEffect(attention) {
        if (!attention || attentionDone) return@LaunchedEffect

        delay(ATTENTION_DELAY.milliseconds)
        launch {
            scale.animateTo(
                targetValue = 1F,
                initialVelocity = ATTENTION_POP_VELOCITY,
                animationSpec = spring(
                    dampingRatio = 0.33F,
                    stiffness = Spring.StiffnessMediumLow,
                ),
            )
        }
        launch {
            rotation.animateTo(
                targetValue = 0F,
                animationSpec = keyframes {
                    durationMillis = 700
                    0F at 0
                    (-18F) at 100
                    16F at 250
                    (-12F) at 400
                    8F at 550
                    0F at 700
                },
            )
        }
        delay(250.milliseconds)
        dotScale.animateTo(
            targetValue = 1F,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow,
            ),
        )
        attentionDone = true
    }

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .size(25.dp)
            .onClick { onViewClick(target) },
    ) {
        Icon(
            painter = painterResource(target.iconRes),
            contentDescription = null,
            tint = TraktTheme.colors.textPrimary,
            modifier = Modifier
                .size(25.dp)
                .graphicsLayer {
                    scaleX = scale.value
                    scaleY = scale.value
                    rotationZ = rotation.value
                    transformOrigin = TransformOrigin(0.5F, 0.5F)
                },
        )

        if (attention) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .graphicsLayer {
                        translationY = -2.dp.toPx()
                        scaleX = dotScale.value
                        scaleY = dotScale.value
                    }
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(TraktTheme.colors.accent),
            )
        }
    }
}

@Preview
@Composable
private fun Preview() {
    TraktTheme {
        Row {
            CalendarViewToggle(current = CalendarView.Weekly)
            CalendarViewToggle(current = CalendarView.Monthly, attention = true)
        }
    }
}
