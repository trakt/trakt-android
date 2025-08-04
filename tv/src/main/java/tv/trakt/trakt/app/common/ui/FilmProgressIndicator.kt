package tv.trakt.trakt.app.common.ui

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.tv.material3.Icon
import tv.trakt.trakt.app.R
import tv.trakt.trakt.app.ui.theme.TraktTheme

@Composable
internal fun FilmProgressIndicator(
    modifier: Modifier = Modifier,
    color: Color = TraktTheme.colors.progressPrimary,
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

@Preview
@Composable
private fun Preview() {
    TraktTheme {
        FilmProgressIndicator()
    }
}
