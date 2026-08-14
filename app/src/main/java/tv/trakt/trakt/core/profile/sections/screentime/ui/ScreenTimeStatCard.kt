package tv.trakt.trakt.core.profile.sections.screentime.ui

import androidx.compose.animation.animateColor
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import tv.trakt.trakt.core.profile.sections.screentime.StatCardSize
import tv.trakt.trakt.ui.theme.TraktTheme

private val cardShape = RoundedCornerShape(16.dp)

@Composable
internal fun ScreenTimeStatCard(
    modifier: Modifier = Modifier,
    value: String,
    label: String,
    subtitle: String,
    subtitleColor: Color = TraktTheme.colors.textSecondary,
    containerColor: Color = TraktTheme.colors.dialogContainer,
) {
    Column(
        verticalArrangement = Arrangement.SpaceBetween,
        modifier = modifier
            .shadow(
                elevation = TraktTheme.colors.shadowDynamicDefault,
                shape = cardShape,
            )
            .background(
                color = containerColor,
                shape = cardShape,
            )
            .padding(horizontal = 14.dp, vertical = 12.dp),
    ) {
        Text(
            text = label,
            style = TraktTheme.typography.cardSubtitle,
            color = TraktTheme.colors.textSecondary,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
        Text(
            text = value,
            style = TraktTheme.typography.heading3.copy(
                fontSize = 22.sp,
                letterSpacing = 0.02.sp,
            ),
            color = TraktTheme.colors.textPrimary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Text(
            text = subtitle,
            style = TraktTheme.typography.meta,
            color = subtitleColor,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
internal fun ScreenTimeStatCardSkeleton(modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "infiniteTransition")
    val shimmerTransition by infiniteTransition
        .animateColor(
            initialValue = TraktTheme.colors.skeletonContainer,
            targetValue = TraktTheme.colors.skeletonShimmer,
            animationSpec = infiniteRepeatable(
                animation = tween(1000),
                repeatMode = RepeatMode.Reverse,
            ),
            label = "shimmerTransition",
        )

    Box(
        modifier = modifier
            .background(
                color = shimmerTransition,
                shape = cardShape,
            ),
    )
}

@Preview(widthDp = 200)
@Composable
private fun ScreenTimeStatCardPreview() {
    TraktTheme {
        ScreenTimeStatCard(
            value = "12h 6m",
            label = "Total Time",
            subtitle = "+1h 30m",
            subtitleColor = Color(0xFF4CAF50),
            modifier = Modifier
                .height(StatCardSize)
                .padding(16.dp),
        )
    }
}
