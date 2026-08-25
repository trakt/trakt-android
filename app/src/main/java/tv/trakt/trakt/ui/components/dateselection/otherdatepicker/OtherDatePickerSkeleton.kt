package tv.trakt.trakt.ui.components.dateselection.otherdatepicker

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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import tv.trakt.trakt.common.helpers.extensions.DeviceSheetPreview
import tv.trakt.trakt.ui.theme.TraktTheme
import tv.trakt.trakt.ui.theme.VerticalImageAspectRatio

@Composable
internal fun OtherDatePickerSkeleton(
    modifier: Modifier = Modifier,
    rows: Int = 12,
    shimmer: Boolean = true,
) {
    val infiniteTransition = rememberInfiniteTransition(label = "infiniteTransition")
    val shimmerTransition by infiniteTransition
        .animateColor(
            initialValue = TraktTheme.colors.dialogContent.copy(
                alpha = 0.33F,
            ),
            targetValue = TraktTheme.colors.dialogContent,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = if (shimmer) 1000 else 0),
                repeatMode = RepeatMode.Reverse,
            ),
            label = "shimmerTransition",
        )

    Column(
        verticalArrangement = spacedBy(0.dp),
        modifier = modifier,
    ) {
        repeat(rows) {
            SkeletonRow(
                color = shimmerTransition,
            )
        }
    }
}

@Composable
private fun SkeletonRow(
    color: Color,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .fillMaxWidth()
                .height(28.dp),
        ) {
            Spacer(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(color),
            )
        }

        Row(
            verticalAlignment = CenterVertically,
            horizontalArrangement = spacedBy(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 2.dp),
        ) {
            Box(
                modifier = Modifier
                    .width(40.dp)
                    .aspectRatio(VerticalImageAspectRatio)
                    .clip(RoundedCornerShape(8.dp))
                    .background(color),
            )

            Column(
                verticalArrangement = spacedBy(6.dp),
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.6F)
                        .height(14.dp)
                        .clip(RoundedCornerShape(100))
                        .background(color),
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.35F)
                        .height(10.dp)
                        .clip(RoundedCornerShape(100))
                        .background(color),
                )
            }
        }
    }
}

@DeviceSheetPreview
@Composable
private fun Preview() {
    TraktTheme {
        OtherDatePickerSkeleton(
            rows = 4,
            modifier = Modifier.padding(horizontal = 24.dp),
        )
    }
}
