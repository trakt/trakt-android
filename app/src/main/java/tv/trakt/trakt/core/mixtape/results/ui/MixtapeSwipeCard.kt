package tv.trakt.trakt.core.mixtape.results.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import tv.trakt.trakt.common.model.mixtape.Mixtape.Suggestion
import tv.trakt.trakt.common.model.mixtape.MixtapeItem
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.theme.TraktTheme

private const val FLIP_DURATION_MS = 400
private const val TILT_DP_PER_DEGREE = 18F
private const val TINT_ALPHA = 0.35F
private const val STAMP_TILT = 12F
private const val CAMERA_DISTANCE = 12F

internal val SwipeCardShape = RoundedCornerShape(20.dp)

@Composable
internal fun MixtapeSwipeCard(
    suggestion: Suggestion,
    offsetX: Float,
    commitDistance: Float,
    flipped: Boolean,
    modifier: Modifier = Modifier,
) {
    val density = LocalDensity.current
    val flip by animateFloatAsState(
        targetValue = if (flipped) 180F else 0F,
        animationSpec = tween(FLIP_DURATION_MS),
        label = "flip",
    )
    val keepStrength = (offsetX / commitDistance).coerceIn(0F, 1F)
    val skipStrength = (-offsetX / commitDistance).coerceIn(0F, 1F)

    Box(
        modifier = modifier.graphicsLayer {
            translationX = offsetX
            rotationZ = offsetX / density.density / TILT_DP_PER_DEGREE
        },
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    rotationY = flip
                    cameraDistance = CAMERA_DISTANCE * density.density
                }
                .clip(SwipeCardShape),
        ) {
            if (flip <= 90F) {
                CardFront(
                    suggestion = suggestion,
                    keepStrength = keepStrength,
                    skipStrength = skipStrength,
                )
            } else {
                CardBack(
                    item = suggestion.item,
                    modifier = Modifier.graphicsLayer { rotationY = 180F },
                )
            }
        }

        CardStamp(
            text = stringResource(R.string.button_text_watchlist),
            color = TraktTheme.colors.sentimentsGoodAccent,
            tilt = -STAMP_TILT,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(20.dp)
                .graphicsLayer { alpha = keepStrength },
        )
        CardStamp(
            text = stringResource(R.string.button_text_mixtape_not_this),
            color = TraktTheme.colors.sentimentsBadAccent,
            tilt = STAMP_TILT,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(20.dp)
                .graphicsLayer { alpha = skipStrength },
        )
    }
}

@Composable
private fun CardFront(
    suggestion: Suggestion,
    keepStrength: Float,
    skipStrength: Float,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(TraktTheme.colors.placeholderContainer),
    ) {
        AsyncImage(
            model = suggestion.item.images?.getPosterUrl(),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize(),
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(TraktTheme.colors.sentimentsGoodAccent.copy(alpha = TINT_ALPHA * keepStrength)),
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(TraktTheme.colors.sentimentsBadAccent.copy(alpha = TINT_ALPHA * skipStrength)),
        )
        Text(
            text = stringResource(R.string.tag_mixtape_match, suggestion.match.toString()),
            style = TraktTheme.typography.meta,
            color = Color.White,
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(12.dp)
                .background(Color.Black.copy(alpha = 0.6F), RoundedCornerShape(100))
                .padding(horizontal = 10.dp, vertical = 4.dp),
        )
    }
}

@Composable
private fun CardBack(
    item: MixtapeItem,
    modifier: Modifier = Modifier,
) {
    Column(
        verticalArrangement = spacedBy(8.dp),
        modifier = modifier
            .fillMaxSize()
            .background(TraktTheme.colors.navigationContainer)
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
    ) {
        Text(
            text = item.title,
            style = TraktTheme.typography.heading5,
            color = TraktTheme.colors.textPrimary,
        )
        Text(
            text = rememberMixtapeMeta(item),
            style = TraktTheme.typography.paragraphSmall,
            color = TraktTheme.colors.textSecondary,
        )
        item.overview?.let {
            Text(
                text = it,
                style = TraktTheme.typography.paragraphSmall,
                color = TraktTheme.colors.textPrimary,
            )
        }
    }
}

@Composable
private fun CardStamp(
    text: String,
    color: Color,
    tilt: Float,
    modifier: Modifier = Modifier,
) {
    Text(
        text = text.uppercase(),
        style = TraktTheme.typography.heading6,
        color = color,
        modifier = modifier
            .graphicsLayer { rotationZ = tilt }
            .border(2.dp, color, RoundedCornerShape(8.dp))
            .padding(horizontal = 10.dp, vertical = 4.dp),
    )
}
