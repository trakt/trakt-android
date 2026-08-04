package tv.trakt.trakt.ui.components.buttons

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults.buttonColors
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathMeasure
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import tv.trakt.trakt.common.ui.composables.FilmProgressIndicator
import tv.trakt.trakt.ui.theme.TraktTheme
import kotlin.time.Duration.Companion.milliseconds

private val HoldDuration = 1650.milliseconds

@Composable
internal fun PrimaryHoldButton(
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
    text: String,
    textStyle: TextStyle = TraktTheme.typography.buttonPrimary,
    icon: Painter? = null,
    iconSize: Dp = 18.dp,
    iconSpace: Dp = 8.dp,
    enabled: Boolean = true,
    loading: Boolean = false,
    height: Dp = 46.dp,
    corner: Dp = 16.dp,
    contentPadding: Dp = 14.dp,
    containerColor: Color = TraktTheme.colors.primaryButtonContainer,
    contentColor: Color = TraktTheme.colors.primaryButtonContent,
    disabledContainerColor: Color = TraktTheme.colors.primaryButtonContainerDisabled,
    disabledContentColor: Color = TraktTheme.colors.primaryButtonContentDisabled,
    holdBorderColor: Color = TraktTheme.colors.primaryButtonContent,
    holdBorderWidth: Dp = 4.dp,
) {
    val progress = remember { Animatable(0F) }
    val scope = rememberCoroutineScope()

    val holdModifier = if (enabled && !loading) {
        Modifier.pointerInput(Unit) {
            awaitEachGesture {
                awaitFirstDown(requireUnconsumed = false)
                val animationJob = scope.launch {
                    progress.snapTo(0f)
                    progress.animateTo(
                        targetValue = 1f,
                        animationSpec = tween(
                            durationMillis = HoldDuration.inWholeMilliseconds.toInt(),
                            easing = LinearEasing,
                        ),
                    )
                    onClick()
                }
                waitForUpOrCancellation()
                animationJob.cancel()
                scope.launch { progress.snapTo(0f) }
            }
        }
    } else {
        Modifier
    }

    Button(
        contentPadding = PaddingValues(
            start = contentPadding,
            end = contentPadding,
        ),
        modifier = modifier
            .height(height)
            .then(holdModifier)
            .drawWithContent {
                drawContent()
                if (progress.value > 0f) {
                    val strokeWidth = holdBorderWidth.toPx()
                    val halfStroke = strokeWidth / 2f
                    val cornerPx = corner.toPx()

                    val path = Path().apply {
                        addRoundRect(
                            RoundRect(
                                rect = Rect(
                                    offset = Offset(0F, 0F),
                                    size = Size(
                                        size.width,
                                        size.height,
                                    ),
                                ),
                                cornerRadius = CornerRadius(cornerPx),
                            ),
                        )
                    }

                    val pathMeasure = PathMeasure()
                    pathMeasure.setPath(path, forceClosed = true)
                    val totalLength = pathMeasure.length

                    val animatedPath = Path()
                    val lengthToDraw = totalLength * progress.value

                    // Draw counter-clockwise (backwards from start)

                    val cornerArcLength = ((Math.PI / 2) * cornerPx).toFloat()
                    val itemWidthNoCorners = size.width - (cornerPx * 2)
                    val startOffset = totalLength - cornerArcLength - (itemWidthNoCorners / 2)

                    val endPoint = startOffset
                    val startPoint = startOffset - lengthToDraw

                    val segment1 = Path()
                    val segment2 = Path()

                    if (startPoint >= 0f) {
                        pathMeasure.getSegment(
                            startDistance = startPoint,
                            stopDistance = endPoint,
                            destination = animatedPath,
                            startWithMoveTo = true,
                        )
                    } else {
                        // Handle wrap-around - draw in two segments
                        pathMeasure.getSegment(
                            startDistance = totalLength + startPoint,
                            stopDistance = totalLength,
                            destination = segment1,
                            startWithMoveTo = true,
                        )
                        pathMeasure.getSegment(
                            startDistance = 0f,
                            stopDistance = endPoint,
                            destination = segment2,
                            startWithMoveTo = true,
                        )

                        animatedPath.addPath(segment1)
                        animatedPath.addPath(segment2)
                    }

                    drawPath(
                        path = animatedPath,
                        color = holdBorderColor,
                        style = Stroke(
                            width = strokeWidth,
                            cap = StrokeCap.Round,
                        ),
                    )
                }
            },
        shape = RoundedCornerShape(corner),
        colors = buttonColors(
            containerColor = containerColor,
            contentColor = contentColor,
            disabledContainerColor = disabledContainerColor,
            disabledContentColor = disabledContentColor,
        ),
        onClick = { /* Handled by pointerInput */ },
        enabled = enabled,
    ) {
        Row(
            verticalAlignment = CenterVertically,
        ) {
            when {
                loading -> {
                    FilmProgressIndicator(
                        size = iconSize,
                        color = if (enabled) contentColor else disabledContentColor,
                        modifier = Modifier
                            .padding(end = iconSpace),
                    )
                }

                icon != null -> {
                    Image(
                        painter = icon,
                        contentDescription = null,
                        contentScale = ContentScale.Fit,
                        colorFilter = ColorFilter.tint(if (enabled) contentColor else disabledContentColor),
                        modifier = Modifier
                            .padding(end = iconSpace)
                            .requiredSize(iconSize),
                    )
                }
            }

            Text(
                text = text,
                color = if (enabled) contentColor else disabledContentColor,
                style = textStyle,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = if (icon != null || loading) TextAlign.Start else TextAlign.Center,
            )
        }
    }
}

@Preview(locale = "us")
@Composable
private fun Preview1() {
    TraktTheme {
        PrimaryHoldButton(
            text = "Hold to Confirm",
        )
    }
}
