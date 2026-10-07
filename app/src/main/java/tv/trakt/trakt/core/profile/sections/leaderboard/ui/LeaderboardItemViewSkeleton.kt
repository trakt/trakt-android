package tv.trakt.trakt.core.profile.sections.leaderboard.ui

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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import tv.trakt.trakt.ui.theme.TraktTheme

@Composable
internal fun LeaderboardItemViewSkeleton(
    modifier: Modifier = Modifier,
    containerColor: Color = TraktTheme.colors.panelCardContainer,
) {
    val infiniteTransition = rememberInfiniteTransition(label = "infiniteTransition")
    val shimmerTransition by infiniteTransition
        .animateColor(
            initialValue = containerColor,
            targetValue = TraktTheme.colors.dialogOnContainer,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 1000),
                repeatMode = RepeatMode.Reverse,
            ),
            label = "shimmerTransition",
        )

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = spacedBy(12.dp),
        modifier = modifier
            .background(containerColor, RoundedCornerShape(16.dp))
            .padding(horizontal = 12.dp, vertical = 10.dp),
    ) {
        Box(
            modifier = Modifier
                .size(width = 32.dp, height = 16.dp)
                .clip(RoundedCornerShape(100))
                .background(shimmerTransition),
        )

        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(shimmerTransition),
        )

        Column(
            verticalArrangement = spacedBy(4.dp),
            modifier = Modifier.weight(1F),
        ) {
            Text(
                text = "",
                style = TraktTheme.typography.cardTitle,
                modifier = Modifier
                    .fillMaxWidth(0.6F)
                    .clip(RoundedCornerShape(100))
                    .background(shimmerTransition),
            )
            Text(
                text = "",
                style = TraktTheme.typography.meta,
                modifier = Modifier
                    .fillMaxWidth(0.4F)
                    .clip(RoundedCornerShape(100))
                    .background(shimmerTransition),
            )
        }
    }
}

@Preview(
    backgroundColor = 0xFF131517,
    showBackground = true,
)
@Composable
private fun Preview() {
    TraktTheme {
        LeaderboardItemViewSkeleton()
    }
}
