package tv.trakt.trakt.core.streamings.ui

import androidx.compose.animation.animateColor
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import tv.trakt.trakt.common.helpers.extensions.DevicePreview
import tv.trakt.trakt.ui.theme.TraktTheme

@Composable
internal fun AllStreamingsSkeletonRow(
    modifier: Modifier = Modifier,
    tiles: Int = 4,
) {
    val infiniteTransition = rememberInfiniteTransition(label = "infiniteTransition")
    val shimmerTransition by infiniteTransition
        .animateColor(
            initialValue = TraktTheme.colors.skeletonContainer,
            targetValue = TraktTheme.colors.skeletonShimmer,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 1000),
                repeatMode = RepeatMode.Reverse,
            ),
            label = "shimmerTransition",
        )

    Column(
        verticalArrangement = spacedBy(TraktTheme.spacing.mainRowSpace),
        modifier = modifier,
    ) {
        repeat(tiles) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(76.dp)
                    .clip(GroupShape)
                    .background(shimmerTransition),
            )
        }
    }
}

@DevicePreview
@Composable
private fun Preview() {
    TraktTheme {
        AllStreamingsSkeletonRow(
            modifier = Modifier.background(TraktTheme.colors.backgroundPrimary),
        )
    }
}
