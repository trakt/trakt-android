package tv.trakt.trakt.core.summary.shows.features.seasons.all.ui

import androidx.compose.animation.animateColor
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import tv.trakt.trakt.common.helpers.extensions.DevicePreview
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.components.EpisodeProgressBar
import tv.trakt.trakt.ui.theme.TraktTheme

@Composable
internal fun SeasonProgressSummary(
    season: Int,
    startText: String,
    endText: String,
    progressPercent: Float,
    modifier: Modifier = Modifier,
) {
    Column(
        verticalArrangement = Arrangement.Absolute.spacedBy(6.dp),
        modifier = modifier,
    ) {
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 2.dp),
        ) {
            Text(
                text = when (season) {
                    0 -> stringResource(R.string.section_title_specials_progress)
                    else -> stringResource(R.string.section_title_season_progress, season)
                },
                style = TraktTheme.typography.cardTitle.copy(fontSize = 14.sp),
                color = TraktTheme.colors.textPrimary,
            )
            Text(
                text = when (progressPercent) {
                    0F -> stringResource(R.string.text_season_not_started)
                    else -> "${(progressPercent * 100).toInt()}%"
                },
                style = TraktTheme.typography.cardTitle.copy(fontSize = 12.sp),
                color = TraktTheme.colors.textSecondary,
            )
        }
        EpisodeProgressBar(
            startText = startText,
            endText = endText,
            progress = progressPercent,
            containerColor = TraktTheme.colors.panelCardContainer,
        )
    }
}

@Composable
internal fun SeasonProgressSummarySkeleton(modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "infiniteTransition")
    val shimmerColor by infiniteTransition.animateColor(
        initialValue = TraktTheme.colors.skeletonContainer,
        targetValue = TraktTheme.colors.skeletonShimmer,
        animationSpec = infiniteRepeatable(
            animation = tween(1000),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "shimmerColor",
    )

    Column(
        verticalArrangement = Arrangement.Absolute.spacedBy(8.dp),
        modifier = modifier,
    ) {
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 1.5.dp),
        ) {
            SkeletonBox(color = shimmerColor, width = 128.dp, height = 14.dp)
            SkeletonBox(color = shimmerColor, width = 32.dp, height = 12.dp)
        }
        SkeletonBox(color = shimmerColor, width = null, height = 26.dp)
    }
}

@Composable
private fun SkeletonBox(
    color: Color,
    height: Dp,
    width: Dp?,
) {
    Box(
        modifier = Modifier
            .then(if (width != null) Modifier.width(width) else Modifier.fillMaxWidth())
            .height(height)
            .background(
                color = color,
                shape = RoundedCornerShape(100),
            ),
    )
}

@DevicePreview
@Composable
private fun Preview() {
    TraktTheme {
        SeasonProgressSummary(
            season = 1,
            startText = "Runtime",
            endText = "Remaining",
            progressPercent = 0.33F,
            modifier = Modifier.padding(16.dp),
        )
    }
}

@DevicePreview
@Composable
private fun SkeletonPreview() {
    TraktTheme {
        SeasonProgressSummarySkeleton(
            modifier = Modifier.padding(16.dp),
        )
    }
}
