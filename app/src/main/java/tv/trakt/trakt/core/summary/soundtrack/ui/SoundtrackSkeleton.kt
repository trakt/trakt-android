package tv.trakt.trakt.core.summary.soundtrack.ui

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
import androidx.compose.material3.ListItemDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import tv.trakt.trakt.common.helpers.extensions.PreviewTraktLightDark
import tv.trakt.trakt.helpers.extensions.TraktThemePreview
import tv.trakt.trakt.ui.theme.TraktTheme

@Composable
internal fun SoundtrackSkeleton(
    rows: Int,
    modifier: Modifier = Modifier,
    panelSize: SoundtrackPanelSize = SoundtrackPanelSize.Compact,
    visible: Boolean = true,
) {
    val color = rememberShimmerColor()

    Column(
        verticalArrangement = spacedBy(10.dp),
        modifier = modifier.alpha(if (visible) 1F else 0F),
    ) {
        SkeletonBlock(color = color, shape = PanelShape, height = panelSize.height)

        Column(verticalArrangement = spacedBy(ListItemDefaults.SegmentedGap)) {
            repeat(rows) { index ->
                SkeletonBlock(
                    color = color,
                    shape = segmentShape(index = index, last = rows - 1, innerCorner = SegmentInnerCorner),
                    height = RowMinHeight,
                )
            }
        }
    }
}

@Composable
internal fun SoundtrackPanelSkeleton(
    height: Dp,
    modifier: Modifier = Modifier,
) {
    SkeletonBlock(
        color = rememberShimmerColor(),
        shape = PanelShape,
        height = height,
        modifier = modifier,
    )
}

@Composable
private fun SkeletonBlock(
    color: Color,
    shape: Shape,
    height: Dp,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .background(color, shape),
    )
}

@Composable
private fun rememberShimmerColor(): Color {
    val color by rememberInfiniteTransition(label = "soundtrackSkeleton")
        .animateColor(
            initialValue = TraktTheme.colors.skeletonContainer,
            targetValue = TraktTheme.colors.skeletonShimmer,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 1000),
                repeatMode = RepeatMode.Reverse,
            ),
            label = "soundtrackSkeletonColor",
        )
    return color
}

@PreviewTraktLightDark
@Composable
private fun Preview() {
    TraktThemePreview {
        SoundtrackSkeleton(rows = 5)
    }
}
