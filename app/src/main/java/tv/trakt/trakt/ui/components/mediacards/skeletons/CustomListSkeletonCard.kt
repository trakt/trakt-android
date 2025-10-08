package tv.trakt.trakt.ui.components.mediacards.skeletons

import androidx.compose.animation.animateColor
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement.Absolute.spacedBy
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults.cardColors
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil3.annotation.ExperimentalCoilApi
import tv.trakt.trakt.ui.theme.HorizontalImageAspectRatio
import tv.trakt.trakt.ui.theme.TraktTheme

@Composable
internal fun CustomListSkeletonCard(
    modifier: Modifier = Modifier,
    shimmer: Boolean = true,
) {
    CustomListSkeletonCardContent(
        modifier = modifier,
        shimmer = shimmer,
    )
}

@Composable
private fun CustomListSkeletonCardContent(
    modifier: Modifier = Modifier,
    shimmer: Boolean,
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

    Card(
        onClick = {},
        modifier = modifier,
        shape = RoundedCornerShape(24.dp),
        colors = cardColors(
            containerColor = shimmerTransition,
        ),
        content = {
            CustomListContent()
        },
    )
}

@Composable
private fun CustomListContent() {
    Column(
        verticalArrangement = spacedBy(0.dp, Alignment.CenterVertically),
        modifier =
            Modifier
                .fillMaxSize()
                .padding(vertical = 16.dp),
    ) {
        CustomListHeader(
            modifier = Modifier.padding(horizontal = 16.dp),
        )

        Box(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(
                        top = 16.dp,
                        start = 16.dp,
                        end = 16.dp,
                    )
                    .background(
                        color = TraktTheme.colors.skeletonShimmer,
                        shape = RoundedCornerShape(12.dp),
                    ),
        )

        Spacer(modifier = Modifier.weight(1F))
    }
}

@Composable
private fun CustomListHeader(modifier: Modifier = Modifier) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = spacedBy(8.dp),
        modifier = modifier,
    ) {
        Box(
            modifier = Modifier
                .background(
                    color = TraktTheme.colors.skeletonShimmer,
                    shape = CircleShape,
                )
                .size(36.dp),
        )

        Column(verticalArrangement = spacedBy(3.dp)) {
            Text(
                text = "",
                modifier = Modifier
                    .fillMaxWidth(0.33F)
                    .background(
                        color = TraktTheme.colors.skeletonShimmer,
                        shape = RoundedCornerShape(100),
                    ),
            )
            Row(
                horizontalArrangement = spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "",
                    modifier = Modifier
                        .fillMaxWidth(0.25F)
                        .background(
                            color = TraktTheme.colors.skeletonShimmer,
                            shape = RoundedCornerShape(100),
                        ),
                )
            }
        }
    }
}

@OptIn(ExperimentalCoilApi::class)
@Preview
@Composable
private fun Preview() {
    TraktTheme {
        Column(
            verticalArrangement = spacedBy(16.dp),
        ) {
            CustomListSkeletonCardContent(
                shimmer = true,
                modifier = Modifier
                    .height(TraktTheme.size.customListCardSize)
                    .aspectRatio(HorizontalImageAspectRatio),
            )
        }
    }
}
