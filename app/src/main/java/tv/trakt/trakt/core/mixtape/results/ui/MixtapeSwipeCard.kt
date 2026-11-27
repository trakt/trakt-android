package tv.trakt.trakt.core.mixtape.results.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import tv.trakt.trakt.common.helpers.extensions.PreviewTraktLightDark
import tv.trakt.trakt.common.helpers.preview.PreviewData
import tv.trakt.trakt.common.model.Images.Size
import tv.trakt.trakt.common.model.mixtape.Mixtape.Suggestion
import tv.trakt.trakt.common.model.mixtape.MixtapeItem
import tv.trakt.trakt.common.ui.theme.colors.Red400
import tv.trakt.trakt.helpers.extensions.TraktThemePreview
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.theme.TraktTheme

private const val FLIP_DURATION_MS = 300
private const val TILT_DP_PER_DEGREE = 18F
private const val TINT_ALPHA = 0.35F
private const val STAMP_TILT = 8F
private const val CAMERA_DISTANCE = 12F

internal val SwipeCardShape = RoundedCornerShape(20.dp)
private val StampStroke = 2.dp
private val StampCorner = 8.dp

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
            color = TraktTheme.colors.accent,
            tilt = -STAMP_TILT,
            alpha = keepStrength,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(20.dp),
        )
        CardStamp(
            text = stringResource(R.string.button_text_mixtape_not_this),
            color = Red400,
            tilt = STAMP_TILT,
            alpha = skipStrength,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(20.dp),
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
            model = suggestion.item.images?.getPosterUrl(Size.MEDIUM),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize(),
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(TraktTheme.colors.accent.copy(alpha = TINT_ALPHA * keepStrength)),
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
                .align(Alignment.BottomEnd)
                .padding(12.dp)
                .background(Color.Black.copy(alpha = 0.6F), RoundedCornerShape(100))
                .padding(horizontal = 12.dp, vertical = 6.dp),
        )
    }
}

@Composable
private fun CardBack(
    item: MixtapeItem,
    modifier: Modifier = Modifier,
) {
    Column(
        verticalArrangement = spacedBy(10.dp),
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
                style = TraktTheme.typography.paragraphSmaller,
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
    alpha: Float,
    modifier: Modifier = Modifier,
) {
    Text(
        text = text.uppercase(),
        style = TraktTheme.typography.heading6,
        color = color,
        modifier = modifier
            .graphicsLayer {
                rotationZ = tilt
                this.alpha = alpha
            }
            .drawBehind {
                val stroke = StampStroke.toPx()
                drawRoundRect(
                    color = color,
                    topLeft = Offset(stroke / 2, stroke / 2),
                    size = size.copy(width = size.width - stroke, height = size.height - stroke),
                    cornerRadius = CornerRadius(StampCorner.toPx()),
                    style = Stroke(stroke),
                )
            }
            .padding(horizontal = 10.dp, vertical = 4.dp),
    )
}

@PreviewTraktLightDark
@Composable
private fun FrontPreview() {
    TraktThemePreview {
        MixtapeSwipeCard(
            suggestion = previewSuggestion,
            offsetX = 0F,
            commitDistance = 300F,
            flipped = false,
            modifier = Modifier
                .width(240.dp)
                .aspectRatio(2F / 3F),
        )
    }
}

@PreviewTraktLightDark
@Composable
private fun KeepDragPreview() {
    TraktThemePreview {
        MixtapeSwipeCard(
            suggestion = previewSuggestion,
            offsetX = 200F,
            commitDistance = 300F,
            flipped = false,
            modifier = Modifier
                .width(240.dp)
                .aspectRatio(2F / 3F),
        )
    }
}

@PreviewTraktLightDark
@Composable
private fun SkipDragPreview() {
    TraktThemePreview {
        MixtapeSwipeCard(
            suggestion = previewSuggestion,
            offsetX = -200F,
            commitDistance = 300F,
            flipped = false,
            modifier = Modifier
                .width(240.dp)
                .aspectRatio(2F / 3F),
        )
    }
}

@PreviewTraktLightDark
@Composable
private fun BackPreview() {
    TraktThemePreview {
        MixtapeSwipeCard(
            suggestion = previewSuggestion,
            offsetX = 0F,
            commitDistance = 300F,
            flipped = true,
            modifier = Modifier
                .width(240.dp)
                .aspectRatio(2F / 3F),
        )
    }
}

private val previewSuggestion = Suggestion(
    item = MixtapeItem.ShowItem(PreviewData.show1),
    because = PreviewData.show2.ids.trakt,
    match = 88,
)
