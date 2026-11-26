package tv.trakt.trakt.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TooltipAnchorPosition
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults.rememberTooltipPositionProvider
import androidx.compose.material3.rememberTooltipState
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
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import tv.trakt.trakt.common.helpers.extensions.PreviewTraktLightDark
import tv.trakt.trakt.helpers.extensions.TraktThemePreview
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.theme.TraktTheme
import kotlin.time.Duration.Companion.milliseconds

private const val ATTENTION_DELAY = 400L
private const val ATTENTION_POP_VELOCITY = 4F
private const val TOOLTIP_DELAY = 300L

private val TOOLTIP_MAX_WIDTH = 240.dp
private val TooltipShape = RoundedCornerShape(8.dp)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun HintIcon(
    painter: Painter,
    modifier: Modifier = Modifier,
    attention: Boolean = false,
    tooltip: String? = null,
    size: Dp = 24.dp,
) {
    val tooltipState = rememberTooltipState(isPersistent = true)

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

        if (tooltip != null) {
            delay(TOOLTIP_DELAY.milliseconds)
            tooltipState.show()
        }
    }

    if (tooltip == null) {
        HintIconContent(
            painter = painter,
            attention = attention,
            size = size,
            scale = scale.value,
            rotation = rotation.value,
            dotScale = dotScale.value,
            modifier = modifier,
        )
        return
    }

    TooltipBox(
        state = tooltipState,
        positionProvider = rememberTooltipPositionProvider(
            positioning = TooltipAnchorPosition.Below,
        ),
        tooltip = {
            Text(
                text = tooltip,
                style = TraktTheme.typography.meta,
                color = TraktTheme.colors.tooltipContent,
                modifier = Modifier
                    .widthIn(max = TOOLTIP_MAX_WIDTH)
                    .background(TraktTheme.colors.tooltipContainer, TooltipShape)
                    .padding(horizontal = 12.dp, vertical = 10.dp),
            )
        },
        enableUserInput = false,
        modifier = modifier,
    ) {
        HintIconContent(
            painter = painter,
            attention = attention,
            size = size,
            scale = scale.value,
            rotation = rotation.value,
            dotScale = dotScale.value,
        )
    }
}

@Composable
private fun HintIconContent(
    painter: Painter,
    attention: Boolean,
    size: Dp,
    scale: Float,
    rotation: Float,
    dotScale: Float,
    modifier: Modifier = Modifier,
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier.size(size),
    ) {
        Icon(
            painter = painter,
            contentDescription = null,
            tint = TraktTheme.colors.textPrimary,
            modifier = Modifier
                .size(size)
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                    rotationZ = rotation
                    transformOrigin = TransformOrigin(0.5F, 0.5F)
                },
        )

        if (attention) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .graphicsLayer {
                        translationY = -2.dp.toPx()
                        scaleX = dotScale
                        scaleY = dotScale
                    }
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(TraktTheme.colors.accent),
            )
        }
    }
}

@PreviewTraktLightDark
@Composable
private fun Preview() {
    TraktThemePreview {
        Row {
            HintIcon(painter = painterResource(R.drawable.ic_mixtape))
            HintIcon(painter = painterResource(R.drawable.ic_mixtape), attention = true)
            HintIcon(
                painter = painterResource(R.drawable.ic_mixtape),
                attention = true,
                tooltip = stringResource(R.string.text_mixtape_strip_description),
            )
        }
    }
}
