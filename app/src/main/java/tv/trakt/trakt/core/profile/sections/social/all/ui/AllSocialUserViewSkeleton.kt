package tv.trakt.trakt.core.profile.sections.social.all.ui

import androidx.compose.animation.animateColor
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement.Absolute.spacedBy
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import tv.trakt.trakt.ui.theme.TraktTheme

@Composable
internal fun AllSocialUserViewSkeleton(
    modifier: Modifier = Modifier,
    size: Dp = 56.dp,
    corner: Dp = 16.dp,
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

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = spacedBy(10.dp),
        modifier = modifier
            .background(containerColor, RoundedCornerShape(corner))
            .padding(horizontal = 12.dp)
            .padding(top = 12.dp, bottom = 10.dp),
    ) {
        Box(
            content = {},
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(size)
                .clip(CircleShape)
                .background(shimmerTransition),
        )

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = spacedBy(2.dp),
        ) {
            Text(
                text = "",
                style = TraktTheme.typography.cardTitle,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(100))
                    .background(shimmerTransition),
            )

            Text(
                text = "",
                style = TraktTheme.typography.cardSubtitle.copy(
                    fontSize = 10.sp,
                ),
                modifier = Modifier
                    .fillMaxWidth(0.6F)
                    .clip(RoundedCornerShape(100))
                    .background(shimmerTransition),
            )
        }
    }
}

@Preview(
    backgroundColor = 0xFF000000,
    showBackground = true,
)
@Composable
private fun Preview() {
    TraktTheme {
        AllSocialUserViewSkeleton()
    }
}
