package tv.trakt.trakt.core.summary.shows.features.progress.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import tv.trakt.trakt.common.helpers.extensions.PreviewTraktLightDark
import tv.trakt.trakt.helpers.extensions.TraktThemePreview
import tv.trakt.trakt.ui.theme.TraktTheme

internal enum class WatchProgressSize(
    val height: Dp,
) {
    Regular(6.dp),
    Large(10.dp),
}

@Composable
internal fun WatchProgressLine(
    progress: Float,
    modifier: Modifier = Modifier,
    lit: Boolean = false,
    size: WatchProgressSize = WatchProgressSize.Regular,
) {
    val shape = RoundedCornerShape(100)

    val fraction by animateFloatAsState(
        targetValue = progress.coerceIn(0F, 1F),
        label = "fraction",
    )
    val fillColor by animateColorAsState(
        targetValue = when {
            lit -> TraktTheme.colors.accent
            else -> TraktTheme.colors.textSecondary
        },
        label = "fillColor",
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(size.height)
            .clip(shape)
            .background(TraktTheme.colors.chipContainer),
    ) {
        if (fraction > 0F) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(fraction)
                    .fillMaxHeight()
                    .background(fillColor, shape),
            )
        }
    }
}

@PreviewTraktLightDark
@Composable
private fun Preview() {
    TraktThemePreview {
        Column(
            verticalArrangement = spacedBy(16.dp),
            modifier = Modifier.padding(16.dp),
        ) {
            WatchProgressLine(progress = 0.4F, lit = true, size = WatchProgressSize.Large)
            WatchProgressLine(progress = 0.7F, lit = true)
            WatchProgressLine(progress = 1F)
            WatchProgressLine(progress = 0F)
        }
    }
}
