package tv.trakt.trakt.app.common.ui.mediacards

import androidx.compose.animation.animateColor
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Arrangement.Absolute.spacedBy
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.tv.material3.Border
import androidx.tv.material3.Card
import androidx.tv.material3.CardDefaults
import androidx.tv.material3.Text
import tv.trakt.trakt.app.ui.theme.TraktTheme

@Composable
internal fun VerticalMediaSkeletonCard(
    modifier: Modifier = Modifier,
    width: Dp = Dp.Unspecified,
    corner: Dp = 12.dp,
    shimmer: Boolean = true,
    footer1: Boolean = true,
    footer2: Boolean = false,
) {
    val infiniteTransition = rememberInfiniteTransition(label = "infiniteTransition")
    val shimmerTransition by infiniteTransition
        .animateColor(
            initialValue = TraktTheme.colors.skeletonContainer,
            targetValue = TraktTheme.colors.skeletonShimmer,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = if (shimmer) 1000 else 0),
                repeatMode = RepeatMode.Reverse,
            ),
            label = "shimmerTransition",
        )

    val cardWidth = when {
        width != Dp.Unspecified -> width
        else -> TraktTheme.size.verticalMediaCardSize
    }

    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = modifier
            .widthIn(max = cardWidth),
    ) {
        Card(
            onClick = { },
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(CardDefaults.VerticalImageAspectRatio),
            shape = CardDefaults.shape(
                shape = RoundedCornerShape(corner),
            ),
            border = CardDefaults.border(
                focusedBorder = Border(
                    border = BorderStroke(width = (2.75).dp, color = TraktTheme.colors.accent),
                    shape = RoundedCornerShape(corner),
                ),
            ),
            colors = CardDefaults.colors(
                containerColor = shimmerTransition,
                focusedContainerColor = shimmerTransition,
            ),
            scale = CardDefaults.scale(
                focusedScale = 1.04f,
            ),
            content = {
                Box(modifier = Modifier.fillMaxSize())
            },
        )

        Column(
            verticalArrangement = spacedBy(1.dp),
        ) {
            if (footer1) {
                Text(
                    text = "Text",
                    style = TraktTheme.typography.cardTitle,
                    color = Color.Transparent,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier
                        .fillMaxWidth(0.75F)
                        .background(color = shimmerTransition, shape = RoundedCornerShape(100)),
                )
            }

            if (footer2) {
                Text(
                    text = "Text",
                    style = TraktTheme.typography.cardSubtitle,
                    color = Color.Transparent,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier
                        .graphicsLayer {
                            translationY = 1.dp.toPx()
                        }
                        .fillMaxWidth(0.5F)
                        .background(color = shimmerTransition, shape = RoundedCornerShape(100)),
                )
            }
        }
    }
}

@Preview
@Composable
private fun Preview() {
    TraktTheme {
        VerticalMediaSkeletonCard(
            modifier = Modifier.padding(16.dp),
        )
    }
}
