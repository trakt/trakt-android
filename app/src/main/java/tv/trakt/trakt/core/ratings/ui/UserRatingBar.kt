package tv.trakt.trakt.core.ratings.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.tween
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Arrangement.Absolute.spacedBy
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.compose.koinInject
import tv.trakt.trakt.common.core.tutorials.TutorialsManager
import tv.trakt.trakt.common.core.tutorials.model.TutorialKey.RATING_BAR_SWIPE
import tv.trakt.trakt.common.helpers.extensions.DevicePreview
import tv.trakt.trakt.common.helpers.extensions.onClick
import tv.trakt.trakt.common.model.MediaType
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.model.ratings.UserRating
import tv.trakt.trakt.common.ui.theme.colors.Red400
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.theme.TraktTheme
import kotlin.math.abs

@Composable
internal fun UserRatingBar(
    modifier: Modifier = Modifier,
    rating: UserRating? = null,
    favorite: Boolean = false,
    favoriteVisible: Boolean = true,
    favoriteLoading: Boolean = false,
    size: Dp = 23.dp,
    onRatingDrag: (Boolean) -> Unit = {},
    onRatingClick: (Int) -> Unit = {},
    onRatingRemoveClick: () -> Unit = {},
    onFavoriteClick: () -> Unit = {},
) {
    val scope = rememberCoroutineScope()
    val context = LocalResources.current
    val tutorials = koinInject<TutorialsManager>()

    val stars = remember(rating) {
        rating?.rating?.div(2f) ?: 0f
    }

    var isDragging by remember { mutableStateOf(false) }
    var dragStars by remember { mutableFloatStateOf(0f) }
    val displayStars = if (isDragging) dragStars else stars

    var dragActiveIndex by remember { mutableIntStateOf(-1) }

    val scaleAnimation = remember { Animatable(1f) }
    val lastClickedIndex = remember { mutableIntStateOf(-1) }

    val density = LocalDensity.current
    val starSizePx = with(density) { size.toPx() }
    val spacingPx = with(density) { 8.dp.toPx() }
    val starsWidth = 5 * starSizePx
    val totalWidth = 5 * starSizePx + 4 * spacingPx

    val tutorialX = remember { Animatable(0f) }
    val tutorialAlpha = remember { Animatable(0f) }
    var tutorialDone by remember { mutableStateOf(false) }

    val animatedAlpha: Float by animateFloatAsState(
        when {
            favoriteVisible && !favoriteLoading -> 1f
            favoriteVisible && favoriteLoading -> 0.5f
            else -> 0.0f
        },
        animationSpec = tween(350, delayMillis = 200),
        label = "alpha",
    )

    var ratingAlphaMaskActive by remember { mutableStateOf(false) }
    val ratingAlphaMask: Float by animateFloatAsState(
        targetValue = if (ratingAlphaMaskActive) 0.05F else 1F,
        animationSpec = tween(200),
        label = "alpha",
    )

    LaunchedEffect(Unit) {
        tutorialDone = tutorials.get(RATING_BAR_SWIPE)
    }

    Box(
        modifier = modifier.padding(top = 22.dp),
    ) {
        if (ratingAlphaMaskActive) {
            val ratingText = if (dragStars == 0f) {
                stringResource(R.string.text_no_rating)
            } else {
                val numText = dragStars.toString().removeSuffix(".0")
                val slugText = UserRating.getSlug(dragStars, context)
                "$numText • $slugText"
            }
            Text(
                text = ratingText,
                textAlign = TextAlign.Center,
                color = TraktTheme.colors.textPrimary,
                style = TraktTheme.typography.meta.copy(
                    fontSize = 13.sp,
                ),
                modifier = Modifier
                    .align(Alignment.Center)
                    .graphicsLayer {
                        if (favoriteVisible) {
                            translationX = -16.dp.toPx()
                        }
                        translationY = -44.dp.toPx()
                    },
            )
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = spacedBy(10.dp, CenterHorizontally),
        ) {
            Box {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = spacedBy(8.dp, CenterHorizontally),
                    modifier = Modifier
                        .pointerInput(Unit) {
                            val starUnit = starSizePx + spacingPx

                            fun xToStars(x: Float): Float {
                                if (x < 0f) return 0f
                                val clampedX = x.coerceIn(0f, totalWidth)
                                val starIndex = (clampedX / starUnit).toInt().coerceIn(0, 4)
                                val posInUnit = clampedX - starIndex * starUnit

                                val ratingInStar = when {
                                    posInUnit < starSizePx / 2 -> 0.5f
                                    else -> 1.0f
                                }

                                return (starIndex + ratingInStar).coerceIn(0.5f, 5f)
                            }

                            fun xToIndex(x: Float): Int {
                                if (x < 0f) return -1
                                val clampedX = x.coerceIn(0f, totalWidth)
                                return (clampedX / starUnit).toInt().coerceIn(0, 4)
                            }

                            val touchSlop = viewConfiguration.touchSlop

                            awaitEachGesture {
                                val down = awaitFirstDown(requireUnconsumed = false)
                                val startPosition = down.position

                                // Wait for direction to be determined
                                var activationX = startPosition.x
                                var dragActivated = false
                                var verticalScrollDetected = false
                                while (true) {
                                    val event = awaitPointerEvent()
                                    val change = event.changes
                                        .firstOrNull { it.id == down.id } ?: break
                                    if (!change.pressed) break

                                    val dx = abs(
                                        change.position.x - startPosition.x,
                                    )
                                    val dy = abs(
                                        change.position.y - startPosition.y,
                                    )

                                    if (dx > touchSlop) {
                                        change.consume()
                                        activationX = change.position.x
                                        dragActivated = true
                                        break
                                    }
                                    if (dy > touchSlop) {
                                        // vertical scroll wins
                                        verticalScrollDetected = true
                                        break
                                    }
                                }

                                if (!dragActivated) {
                                    if (!tutorialDone && !verticalScrollDetected) {
                                        scope.launch {
                                            tutorialX.snapTo(starSizePx)
                                            tutorialAlpha.snapTo(0f)
                                            launch {
                                                delay(400)
                                                tutorialX.animateTo(
                                                    targetValue = starsWidth,
                                                    animationSpec = tween(500, easing = LinearEasing),
                                                )
                                            }
                                            tutorialAlpha.animateTo(
                                                targetValue = 1f,
                                                animationSpec = tween(300),
                                            )
                                            delay(800)
                                            tutorialAlpha.animateTo(
                                                targetValue = 0f,
                                                animationSpec = tween(300),
                                            )
                                        }
                                    }
                                    return@awaitEachGesture
                                }

                                isDragging = true
                                onRatingDrag(true)
                                ratingAlphaMaskActive = true
                                dragStars = xToStars(activationX)
                                dragActiveIndex = xToIndex(activationX)

                                while (true) {
                                    val event = awaitPointerEvent()
                                    if (event.changes.all { !it.pressed }) break
                                    event.changes.forEach { change ->
                                        dragStars = xToStars(change.position.x)
                                        dragActiveIndex = xToIndex(change.position.x)
                                        change.consume()
                                    }
                                }

                                isDragging = false
                                onRatingDrag(false)
                                ratingAlphaMaskActive = false

                                if (!tutorialDone) {
                                    tutorialDone = true
                                    scope.launch {
                                        tutorials.acknowledge(RATING_BAR_SWIPE)
                                    }
                                }

                                dragActiveIndex = -1
                                val finalRating = dragStars
                                if (finalRating == 0f) {
                                    onRatingRemoveClick()
                                } else {
                                    onRatingClick(UserRating.scaleTo10(finalRating))
                                }

                                lastClickedIndex.intValue =
                                    (finalRating - 0.5f).toInt().coerceIn(0, 4)
                                runScaleAnimation(
                                    scope = scope,
                                    animation = scaleAnimation,
                                )
                            }
                        },
                ) {
                    repeat(5) { index ->
                        val distance = if (isDragging && dragActiveIndex >= 0) {
                            abs(index - dragActiveIndex)
                        } else {
                            Int.MAX_VALUE
                        }

                        val isLeftNeighbor = isDragging &&
                            dragActiveIndex >= 0 &&
                            index == dragActiveIndex - 1

                        val elevationFraction = when {
                            distance == 0 -> 1f
                            isLeftNeighbor -> 0.33f
                            else -> 0f
                        }

                        val activeScale by animateFloatAsState(
                            targetValue = 1f + 0.35f * elevationFraction,
                            animationSpec = tween(200),
                            label = "activeScale$index",
                        )
                        val activeTranslationY by animateFloatAsState(
                            targetValue = -starSizePx * 0.5f * elevationFraction,
                            animationSpec = tween(200),
                            label = "activeTranslationY$index",
                        )

                        val bounceScale = when {
                            lastClickedIndex.intValue == index -> scaleAnimation.value
                            else -> 1f
                        }

                        Icon(
                            painter = painterResource(
                                when {
                                    displayStars >= index + 1 -> R.drawable.ic_star_trakt_on
                                    displayStars >= index + 0.5F -> R.drawable.ic_star_trakt_half
                                    else -> R.drawable.ic_star_trakt_off
                                },
                            ),
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier
                                .size(size)
                                .graphicsLayer {
                                    scaleX = activeScale * bounceScale
                                    scaleY = activeScale * bounceScale
                                    translationY = activeTranslationY
                                },
                        )
                    }
                }

                if (!tutorialDone && tutorialAlpha.value > 0f) {
                    Icon(
                        painter = painterResource(R.drawable.ic_touch),
                        contentDescription = null,
                        tint = Red400,
                        modifier = Modifier
                            .size(size)
                            .graphicsLayer {
                                rotationZ = -45F
                                translationX = tutorialX.value
                                alpha = tutorialAlpha.value
                            },
                    )
                }
            }

            if (favoriteVisible) {
                val scale = when {
                    lastClickedIndex.intValue == -1 -> scaleAnimation.value
                    else -> 1f
                }
                Icon(
                    painter = painterResource(
                        when {
                            favorite -> R.drawable.ic_heart_on
                            else -> R.drawable.ic_heart_off
                        },
                    ),
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier
                        .size(size)
                        .alpha(if (ratingAlphaMaskActive) ratingAlphaMask else animatedAlpha)
                        .graphicsLayer {
                            scaleX = scale
                            scaleY = scale
                        }
                        .onClick {
                            if (!favoriteLoading) {
                                lastClickedIndex.intValue = -1

                                onFavoriteClick()

                                runScaleAnimation(
                                    scope = scope,
                                    animation = scaleAnimation,
                                )
                            }
                        },
                )
            }
        }
    }
}

private fun runScaleAnimation(
    scope: CoroutineScope,
    animation: Animatable<Float, AnimationVector1D>,
) {
    scope.launch {
        animation.animateTo(
            targetValue = 1f,
            initialVelocity = 1f,
            animationSpec = keyframes {
                durationMillis = 350
                1.2f at 100
                0.9f at 250
                1f at 350
            },
        )
    }
}

@DevicePreview
@Composable
private fun Preview() {
    TraktTheme {
        UserRatingBar()
    }
}

@DevicePreview
@Composable
private fun Preview2() {
    TraktTheme {
        UserRatingBar(
            rating = UserRating(
                mediaId = TraktId(1),
                mediaType = MediaType.MOVIE,
                rating = 7,
            ),
        )
    }
}

@DevicePreview
@Composable
private fun Preview3() {
    TraktTheme {
        UserRatingBar(
            favoriteVisible = true,
            favoriteLoading = true,
            rating = UserRating(
                mediaId = TraktId(1),
                mediaType = MediaType.MOVIE,
                rating = 7,
            ),
        )
    }
}
