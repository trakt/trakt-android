package tv.trakt.trakt.core.summary.ui.views.insights

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight.Companion.W600
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import tv.trakt.trakt.common.helpers.extensions.PreviewTraktLightDark
import tv.trakt.trakt.common.model.realitycheck.RealityTier
import tv.trakt.trakt.helpers.extensions.TraktThemePreview
import tv.trakt.trakt.ui.theme.TraktTheme

private const val SEGMENT_OFF_ALPHA = 0.14F
private const val PILL_CONTAINER_ALPHA = 0.14F

@Composable
internal fun RealityTierPill(
    tier: RealityTier,
    color: Color,
    modifier: Modifier = Modifier,
) {
    Row(
        horizontalArrangement = spacedBy(8.dp),
        verticalAlignment = CenterVertically,
        modifier = modifier
            .height(28.dp)
            .background(
                color = color.copy(alpha = PILL_CONTAINER_ALPHA),
                shape = CircleShape,
            )
            .padding(horizontal = 10.dp),
    ) {
        SegmentMeter(
            filled = tier.segments,
            color = color,
        )
        Text(
            text = stringResource(tier.displayTextRes),
            color = color,
            style = TraktTheme.typography.paragraphSmaller.copy(fontWeight = W600),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
internal fun SegmentMeter(
    filled: Int,
    color: Color,
    modifier: Modifier = Modifier,
    large: Boolean = false,
) {
    val offColor = TraktTheme.colors.textPrimary.copy(alpha = SEGMENT_OFF_ALPHA)
    val height: Dp = if (large) 6.dp else 4.dp
    val shape = RoundedCornerShape(100)

    Row(
        horizontalArrangement = spacedBy(if (large) 4.dp else 2.dp),
        verticalAlignment = CenterVertically,
        modifier = if (large) modifier.fillMaxWidth() else modifier,
    ) {
        repeat(RealityTier.SEGMENT_COUNT) { index ->
            Box(
                modifier = Modifier
                    .then(
                        if (large) {
                            Modifier
                                .weight(1F)
                                .height(height)
                        } else {
                            Modifier.size(width = 8.dp, height = height)
                        },
                    )
                    .background(
                        color = if (index < filled) color else offColor,
                        shape = shape,
                    ),
            )
        }
    }
}

@PreviewTraktLightDark
@Composable
private fun Preview() {
    TraktThemePreview {
        Column(
            verticalArrangement = spacedBy(12.dp),
            modifier = Modifier.padding(16.dp),
        ) {
            RealityTier.entries.forEach { tier ->
                RealityTierPill(
                    tier = tier,
                    color = TraktTheme.colors.textPrimary,
                )
            }
            SegmentMeter(
                filled = RealityTier.MostlyTrue.segments,
                color = TraktTheme.colors.textPrimary,
                large = true,
            )
        }
    }
}
