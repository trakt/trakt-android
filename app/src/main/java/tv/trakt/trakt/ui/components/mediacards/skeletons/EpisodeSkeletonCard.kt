package tv.trakt.trakt.ui.components.mediacards.skeletons

import androidx.compose.animation.animateColor
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults.cardColors
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import tv.trakt.trakt.ui.components.InfoChip
import tv.trakt.trakt.ui.theme.HorizontalImageAspectRatio
import tv.trakt.trakt.ui.theme.TraktTheme

@Composable
internal fun EpisodeSkeletonCard(
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
        verticalArrangement = Arrangement.spacedBy(6.dp),
        modifier = modifier
            .sizeIn(maxWidth = TraktTheme.size.horizontalMediaCardSize),
    ) {
        Card(
            onClick = {},
            modifier = Modifier
                .width(TraktTheme.size.horizontalMediaCardSize)
                .aspectRatio(HorizontalImageAspectRatio),
            shape = RoundedCornerShape(15.dp),
            colors = cardColors(
                containerColor = shimmerTransition,
            ),
            content = {
                Box(modifier = Modifier.fillMaxSize()) {
                    InfoChip(
                        text = "",
                        containerColor = TraktTheme.colors.skeletonShimmer,
                        modifier = Modifier
                            .padding(6.dp)
                            .align(Alignment.BottomCenter)
                            .fillMaxWidth(),
                    )
                }
            },
        )

        Column(
            verticalArrangement = Arrangement.spacedBy(3.dp),
        ) {
            Text(
                text = "",
                style = TraktTheme.typography.cardTitle,
                color = TraktTheme.colors.textPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .fillMaxWidth(0.33F)
                    .background(
                        color = shimmerTransition,
                        shape = RoundedCornerShape(100),
                    ),
            )

            Text(
                text = "",
                style = TraktTheme.typography.cardSubtitle,
                color = TraktTheme.colors.textSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .fillMaxWidth(0.66F)
                    .background(
                        color = shimmerTransition,
                        shape = RoundedCornerShape(100),
                    ),
            )
        }
    }
}

@Preview(widthDp = 160)
@Composable
private fun Preview() {
    TraktTheme {
        EpisodeSkeletonCard()
    }
}
