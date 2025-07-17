package tv.trakt.trakt.tv.common.ui.mediacards

import InfoChip
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
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.tv.material3.Border
import androidx.tv.material3.Card
import androidx.tv.material3.CardDefaults
import tv.trakt.trakt.tv.ui.theme.TraktTheme

@Composable
internal fun HorizontalMediaSkeletonCard(
    modifier: Modifier = Modifier,
    shimmer: Boolean = true,
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

    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = modifier
            .sizeIn(maxWidth = TraktTheme.size.horizontalMediaCardSize),
    ) {
        Card(
            onClick = {},
            modifier = Modifier
                .width(TraktTheme.size.horizontalMediaCardSize)
                .aspectRatio(CardDefaults.HorizontalImageAspectRatio),
            shape = CardDefaults.shape(
                shape = RoundedCornerShape(12.dp),
            ),
            border = CardDefaults.border(
                focusedBorder = Border(
                    border = BorderStroke(width = (2.75).dp, color = TraktTheme.colors.accent),
                    shape = RoundedCornerShape(12.dp),
                ),
            ),
            colors = CardDefaults.colors(
                containerColor = shimmerTransition,
                focusedContainerColor = shimmerTransition,
                pressedContainerColor = shimmerTransition,
            ),
            scale = CardDefaults.scale(
                focusedScale = 1.06f,
            ),
            content = {
                Box(modifier = Modifier.fillMaxSize())
            },
        )

        Column(
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            InfoChip(
                text = "",
                containerColor = shimmerTransition,
                modifier = Modifier.fillMaxWidth(0.33F),
            )
        }
    }
}

@Preview(widthDp = 160)
@Composable
private fun Preview() {
    TraktTheme {
        HorizontalMediaSkeletonCard()
    }
}
