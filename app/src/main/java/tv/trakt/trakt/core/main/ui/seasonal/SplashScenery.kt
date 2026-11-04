package tv.trakt.trakt.core.main.ui.seasonal

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

internal data class SplashGroundSize(
    val aspectRatio: Float,
    val minHeight: Dp = 0.dp,
    val maxHeight: Dp,
)

/** A full-width strip hanging from the top edge, cropped at the sides on narrow screens. */
@Composable
internal fun SplashGarland(
    @DrawableRes image: Int,
    height: Dp,
    animate: Boolean,
    modifier: Modifier = Modifier,
) {
    val intro = rememberSplashIntro(animate = animate, delayMillis = 200, durationMillis = 600)

    Image(
        painter = painterResource(image),
        contentDescription = null,
        contentScale = ContentScale.Crop,
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .graphicsLayer {
                alpha = intro.value
            },
    )
}

/** A full-width scene standing on the bottom edge, rising in on entrance. */
@Composable
internal fun SplashGround(
    @DrawableRes image: Int,
    size: SplashGroundSize,
    animate: Boolean,
    modifier: Modifier = Modifier,
) {
    val intro = rememberSplashIntro(
        animate = animate,
        delayMillis = 200,
        durationMillis = 900,
        easing = SplashEaseOutQuint,
    )

    BoxWithConstraints(modifier = modifier.fillMaxWidth()) {
        val height = (maxWidth / size.aspectRatio).coerceIn(size.minHeight, size.maxHeight)

        Image(
            painter = painterResource(image),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            alignment = Alignment.BottomCenter,
            modifier = Modifier
                .fillMaxWidth()
                .height(height)
                .graphicsLayer {
                    alpha = intro.value
                    translationY = (1F - intro.value) * this.size.height * 0.3F
                },
        )
    }
}
