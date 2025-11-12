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
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import tv.trakt.trakt.ui.theme.TraktTheme
import tv.trakt.trakt.ui.theme.VerticalImageAspectRatio

@Composable
internal fun VerticalMediaSkeletonCard(
    modifier: Modifier = Modifier,
    width: Dp = Dp.Unspecified,
    corner: Dp = 15.dp,
    chipRatio: Float = 0.33F,
    secondaryChipRatio: Float = 0.5F,
    shimmer: Boolean = true,
    chip: Boolean = true,
    secondaryChip: Boolean = false,
    containerColor: Color = TraktTheme.colors.skeletonContainer,
    shimmerColor: Color = TraktTheme.colors.skeletonShimmer,
) {
    val infiniteTransition = rememberInfiniteTransition(label = "infiniteTransition")
    val shimmerTransition by infiniteTransition
        .animateColor(
            initialValue = containerColor,
            targetValue = shimmerColor,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 1000),
                repeatMode = RepeatMode.Reverse,
            ),
            label = "shimmerTransition",
        )

    val cardWidth = when {
        width != Dp.Unspecified -> width
        else -> TraktTheme.size.verticalMediaCardSize
    }

    Column(
        verticalArrangement = Arrangement.spacedBy(6.dp),
        modifier = modifier
            .widthIn(max = cardWidth),
    ) {
        Card(
            onClick = { },
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(VerticalImageAspectRatio),
            colors = CardDefaults.cardColors(
                containerColor = when {
                    shimmer -> shimmerTransition
                    else -> containerColor
                },
            ),
            shape = RoundedCornerShape(corner),
            content = {
                Box(modifier = Modifier.fillMaxSize())
            },
        )

        Column(
            verticalArrangement = Arrangement.spacedBy(3.dp),
        ) {
            if (chip) {
                Text(
                    text = "",
                    style = TraktTheme.typography.cardTitle,
                    color = TraktTheme.colors.textPrimary,
                    modifier = Modifier
                        .fillMaxWidth(chipRatio)
                        .height(14.dp)
                        .background(
                            color = when {
                                shimmer -> shimmerTransition
                                else -> containerColor
                            },
                            shape = RoundedCornerShape(100.dp),
                        ),
                )
            }

            if (secondaryChip) {
                Text(
                    text = "",
                    style = TraktTheme.typography.cardSubtitle,
                    color = TraktTheme.colors.textPrimary,
                    modifier = Modifier
                        .fillMaxWidth(secondaryChipRatio)
                        .height(14.dp)
                        .background(
                            color = when {
                                shimmer -> shimmerTransition
                                else -> containerColor
                            },
                            shape = RoundedCornerShape(100.dp),
                        ),
                )
            }
        }
    }
}

@Preview
@Composable
private fun Preview() {
    TraktTheme {
        Column {
            VerticalMediaSkeletonCard(
                modifier = Modifier.padding(16.dp),
            )
            VerticalMediaSkeletonCard(
                modifier = Modifier.padding(16.dp),
                secondaryChip = true,
            )
            VerticalMediaSkeletonCard(
                modifier = Modifier.padding(16.dp),
                chip = false,
            )
        }
    }
}
