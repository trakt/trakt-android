package tv.trakt.trakt.ui.components.whatsnew

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import tv.trakt.trakt.common.helpers.extensions.onClick
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.theme.TraktTheme
import kotlin.time.Duration.Companion.milliseconds

private const val ENTRANCE_DELAY = 400L

@Composable
internal fun WhatsNewIcon(
    modifier: Modifier = Modifier,
    size: Dp = 22.dp,
    onClick: () -> Unit,
) {
    var entranceDone by rememberSaveable { mutableStateOf(false) }
    val scale = remember { Animatable(if (entranceDone) 1F else 0F) }
    val rotation = remember { Animatable(0F) }
    val dotScale = remember { Animatable(if (entranceDone) 1F else 0F) }

    LaunchedEffect(Unit) {
        if (entranceDone) return@LaunchedEffect

        delay(ENTRANCE_DELAY.milliseconds)
        scale.animateTo(
            targetValue = 1F,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessMediumLow,
            ),
        )
        launch {
            rotation.animateTo(
                targetValue = 0F,
                animationSpec = keyframes {
                    durationMillis = 700
                    0F at 0
                    -18F at 100
                    16F at 250
                    -12F at 400
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
        entranceDone = true
    }

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .size(size)
            .onClick(onClick = onClick),
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_stars),
            contentDescription = stringResource(R.string.header_whats_new),
            tint = TraktTheme.colors.textPrimary,
            modifier = Modifier
                .size(size)
                .graphicsLayer {
                    scaleX = scale.value
                    scaleY = scale.value
                    rotationZ = rotation.value
                    transformOrigin = TransformOrigin(0.5F, 0.5F)
                },
        )

        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .graphicsLayer {
                    translationX = 1.dp.toPx()
                    translationY = -1.5.dp.toPx()
                    scaleX = dotScale.value
                    scaleY = dotScale.value
                }
                .size(4.dp)
                .clip(CircleShape)
                .background(TraktTheme.colors.accent),
        )
    }
}

@Preview
@Composable
private fun WhatsNewIconPreview() {
    TraktTheme {
        WhatsNewIcon(
            onClick = {},
            modifier = Modifier.padding(16.dp),
        )
    }
}
