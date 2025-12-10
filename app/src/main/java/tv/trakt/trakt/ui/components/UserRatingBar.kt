package tv.trakt.trakt.ui.components

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement.Absolute.spacedBy
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import tv.trakt.trakt.common.helpers.extensions.onClick
import tv.trakt.trakt.common.model.MediaType
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.model.ratings.UserRating
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.theme.TraktTheme

@Composable
internal fun UserRatingBar(
    modifier: Modifier = Modifier,
    rating: UserRating? = null,
    favorite: Boolean = false,
    favoriteVisible: Boolean = true,
    favoriteLoading: Boolean = false,
    size: Dp = 23.dp,
    onRatingClick: (Int) -> Unit = {},
    onFavoriteClick: () -> Unit = {},
) {
    val scope = rememberCoroutineScope()

    val stars = remember(rating) {
        rating?.rating?.div(2f) ?: 0f
    }

    val scaleAnimation = remember { Animatable(1f) }
    val lastClickedIndex = remember { mutableIntStateOf(-1) }

    val animatedAlpha: Float by animateFloatAsState(
        when {
            favoriteVisible && !favoriteLoading -> 1f
            favoriteVisible && favoriteLoading -> 0.5f
            else -> 0.0f
        },
        animationSpec = tween(350, delayMillis = 200),
        label = "alpha",
    )

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = spacedBy(10.dp, CenterHorizontally),
        modifier = modifier
            .animateContentSize(),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = spacedBy(8.dp, CenterHorizontally),
        ) {
            repeat(5) { index ->
                val scale = when {
                    lastClickedIndex.intValue == index -> scaleAnimation.value
                    else -> 1f
                }
                Icon(
                    painter = painterResource(
                        when {
                            stars >= index + 1 -> R.drawable.ic_star_trakt_on
                            stars >= index + 0.5F -> R.drawable.ic_star_trakt_half
                            else -> R.drawable.ic_star_trakt_off
                        },
                    ),
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier
                        .size(size)
                        .graphicsLayer {
                            scaleX = scale
                            scaleY = scale
                        }
                        .onClick(throttle = false) {
                            val scaledRating = UserRating.scaleTo10(index + 1F)

                            if (rating?.rating != scaledRating) {
                                lastClickedIndex.intValue = index
                                onRatingClick(scaledRating)
                            } else {
                                lastClickedIndex.intValue = index
                                onRatingClick(scaledRating - 1)
                            }

                            runScaleAnimation(
                                scope = scope,
                                animation = scaleAnimation,
                            )
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
                    .alpha(animatedAlpha)
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

@Preview
@Composable
private fun Preview() {
    TraktTheme {
        UserRatingBar()
    }
}

@Preview
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

@Preview
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
