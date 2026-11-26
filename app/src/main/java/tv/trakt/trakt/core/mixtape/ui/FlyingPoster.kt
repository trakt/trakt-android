package tv.trakt.trakt.core.mixtape.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.lerp
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.lerp
import androidx.compose.ui.util.fastRoundToInt
import coil3.compose.AsyncImage
import tv.trakt.trakt.common.model.mixtape.MixtapeItem
import tv.trakt.trakt.ui.theme.TraktTheme

private const val FLIGHT_DURATION_MS = 350

private val StartCorner = 15.dp
private val EndCorner = 8.dp

@Immutable
internal data class PosterFlight(
    val item: MixtapeItem,
    val from: Rect,
    val to: Rect,
)

@Composable
internal fun FlyingPoster(
    flight: PosterFlight,
    onLanded: () -> Unit,
) {
    val density = LocalDensity.current
    val progress = remember { Animatable(0F) }

    LaunchedEffect(flight) {
        progress.animateTo(
            targetValue = 1F,
            animationSpec = tween(
                durationMillis = FLIGHT_DURATION_MS,
                easing = FastOutSlowInEasing,
            ),
        )
        onLanded()
    }

    val bounds = lerp(flight.from, flight.to, progress.value)

    AsyncImage(
        model = flight.item.images?.getPosterUrl(),
        contentDescription = null,
        contentScale = ContentScale.Crop,
        modifier = Modifier
            .offset { IntOffset(bounds.left.fastRoundToInt(), bounds.top.fastRoundToInt()) }
            .size(
                width = with(density) { bounds.width.toDp() },
                height = with(density) { bounds.height.toDp() },
            )
            .clip(RoundedCornerShape(lerp(StartCorner, EndCorner, progress.value)))
            .background(TraktTheme.colors.placeholderContainer),
    )
}
