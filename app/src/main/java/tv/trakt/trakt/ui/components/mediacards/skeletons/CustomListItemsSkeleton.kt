package tv.trakt.trakt.ui.components.mediacards.skeletons

import androidx.compose.animation.animateColor
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import tv.trakt.trakt.common.helpers.extensions.DevicePreview
import tv.trakt.trakt.ui.theme.TraktTheme

@Composable
internal fun CustomListItemsSkeleton(
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues.Zero,
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
        verticalArrangement = spacedBy(TraktTheme.spacing.mainRowHeaderSpace),
        modifier = modifier,
    ) {
        Text(
            text = "",
            style = TraktTheme.typography.cardTitle,
            color = TraktTheme.colors.textPrimary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier
                .padding(contentPadding)
                .height(20.dp)
                .fillMaxWidth(0.5F)
                .background(
                    color = shimmerTransition,
                    shape = RoundedCornerShape(100),
                ),
        )

        LazyRow(
            horizontalArrangement = spacedBy(TraktTheme.spacing.mainRowSpace),
            contentPadding = contentPadding,
            modifier = Modifier.fillMaxWidth(),
        ) {
            items(count = 12) {
                VerticalMediaSkeletonCard(
                    chipRatio = 0.66F,
                    chipSpacing = 8.dp,
                )
            }
        }
    }
}

@DevicePreview
@Composable
private fun Preview() {
    TraktTheme {
        CustomListItemsSkeleton()
    }
}
