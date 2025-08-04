package tv.trakt.trakt.app.common.ui.mediacards

import androidx.compose.animation.animateColor
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.tv.material3.Border
import androidx.tv.material3.Card
import androidx.tv.material3.CardDefaults
import tv.trakt.trakt.app.common.ui.InfoChip
import tv.trakt.trakt.app.ui.theme.TraktTheme

@Composable
internal fun VerticalMediaSkeletonCard(
    modifier: Modifier = Modifier,
    width: Dp = Dp.Unspecified,
    corner: Dp = 12.dp,
    shimmer: Boolean = true,
    chip: Boolean = true,
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
        if (chip) {
            InfoChip(
                text = "",
                containerColor = shimmerTransition,
                modifier = Modifier.fillMaxWidth(0.5F),
            )
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
