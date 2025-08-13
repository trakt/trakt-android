package tv.trakt.trakt.common.ui.composables

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import tv.trakt.trakt.common.R
import tv.trakt.trakt.common.ui.theme.colors.Shade300

@Composable
fun FilmProgressIndicator(
    modifier: Modifier = Modifier,
    color: Color = Shade300,
    size: Dp = 48.dp,
) {
    val animation = rememberInfiniteTransition(label = "FilmProgressIndicatorTransition")
        .animateFloat(
            initialValue = 0f,
            targetValue = 360f,
            animationSpec = infiniteRepeatable(
                animation = tween(1250),
                repeatMode = RepeatMode.Restart,
            ),
            label = "FilmProgressIndicatorRotation",
        )

    Icon(
        painter = painterResource(R.drawable.ic_film_roll),
        contentDescription = null,
        tint = color,
        modifier = modifier
            .size(size)
            .rotate(animation.value),
    )
}
