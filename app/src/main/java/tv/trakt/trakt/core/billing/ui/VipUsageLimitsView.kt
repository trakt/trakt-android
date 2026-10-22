package tv.trakt.trakt.core.billing.ui

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import tv.trakt.trakt.common.helpers.extensions.onClick
import tv.trakt.trakt.common.helpers.extensions.rememberNumberFormat
import tv.trakt.trakt.common.ui.theme.colors.Red500
import tv.trakt.trakt.core.billing.model.VipUsageLimit
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.theme.TraktTheme

private const val COLLAPSED_ITEMS = 3
private const val FREE_ZONE_FRACTION = 0.3F

private val BarHeight = 16.dp
private val VipZoneOverlap = 24.dp

@Composable
internal fun VipUsageLimitsView(
    limits: ImmutableList<VipUsageLimit>,
    modifier: Modifier = Modifier,
) {
    var expanded by rememberSaveable { mutableStateOf(false) }
    val visibleLimits = if (expanded) limits else limits.take(COLLAPSED_ITEMS)

    Column(
        verticalArrangement = spacedBy(16.dp),
        modifier = modifier.animateContentSize(),
    ) {
        Text(
            text = stringResource(R.string.vip_text_vip_go_beyond_limits),
            style = TraktTheme.typography.heading5,
            color = TraktTheme.colors.textPrimary,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
        )

        visibleLimits.forEach { limit ->
            VipUsageLimitItem(limit = limit)
        }

        if (limits.size > COLLAPSED_ITEMS && !expanded) {
            Text(
                text = stringResource(R.string.button_label_see_more_limits),
                style = TraktTheme.typography.buttonSecondary,
                color = TraktTheme.colors.textSecondary,
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .onClick { expanded = true },
            )
        }
    }
}

@Composable
private fun VipUsageLimitItem(limit: VipUsageLimit) {
    Column(
        verticalArrangement = spacedBy(6.dp),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(
                text = stringResource(limit.title),
                style = TraktTheme.typography.paragraphSmall,
                color = TraktTheme.colors.textPrimary,
                modifier = Modifier.weight(1F),
            )
            Text(
                text = "${rememberNumberFormat(limit.current)} / ${rememberNumberFormat(limit.free)}",
                style = TraktTheme.typography.paragraphSmall,
                color = TraktTheme.colors.textSecondary,
            )
        }

        VipUsageBar(limit = limit)
    }
}

@Composable
private fun VipUsageBar(limit: VipUsageLimit) {
    val barColor = TraktTheme.colors.vipAccent
    val fillColor = if (limit.current > limit.free) Red500 else barColor
    val hasVipZone = limit.vip != limit.free

    val zoneFraction = if (hasVipZone) FREE_ZONE_FRACTION else 1F
    val progress = when {
        limit.free <= 0 -> 0F
        else -> (limit.current.toFloat() / limit.free).coerceIn(0F, 1F) * zoneFraction
    }

    Box(
        contentAlignment = Alignment.CenterEnd,
        modifier = Modifier
            .fillMaxWidth()
            .height(BarHeight)
            .drawBehind {
                val radius = CornerRadius(size.height / 2)
                val borderColor = barColor.copy(alpha = 0.3F)

                if (hasVipZone) {
                    val start = size.width * zoneFraction - VipZoneOverlap.toPx()
                    drawZoneBorder(
                        color = borderColor,
                        start = start,
                        end = size.width,
                        radius = radius,
                        dashed = true,
                    )
                }

                val zoneWidth = size.width * zoneFraction
                drawRoundRect(
                    color = barColor.copy(alpha = 0.15F),
                    size = Size(zoneWidth, size.height),
                    cornerRadius = radius,
                )
                drawZoneBorder(
                    color = borderColor,
                    start = 0F,
                    end = zoneWidth,
                    radius = radius,
                    dashed = false,
                )

                if (progress > 0F) {
                    drawRoundRect(
                        color = fillColor,
                        size = Size(size.width * progress, size.height),
                        cornerRadius = radius,
                    )
                }
            },
    ) {
        if (hasVipZone) {
            Text(
                text = rememberNumberFormat(limit.vip),
                style = TraktTheme.typography.meta,
                color = TraktTheme.colors.textSecondary,
                modifier = Modifier.padding(end = 8.dp),
            )
        }
    }
}

private fun DrawScope.drawZoneBorder(
    color: Color,
    start: Float,
    end: Float,
    radius: CornerRadius,
    dashed: Boolean,
) {
    val strokeWidth = 1.dp.toPx()
    val inset = strokeWidth / 2
    drawRoundRect(
        color = color,
        topLeft = Offset(start + inset, inset),
        size = Size(end - start - strokeWidth, size.height - strokeWidth),
        cornerRadius = radius,
        style = Stroke(
            width = strokeWidth,
            pathEffect = if (dashed) PathEffect.dashPathEffect(floatArrayOf(6F, 4F)) else null,
        ),
    )
}

@Preview(widthDp = 380, showBackground = true, backgroundColor = 0xFF131517)
@Composable
private fun Preview() {
    TraktTheme {
        VipUsageLimitsView(
            limits = persistentListOf(
                VipUsageLimit(R.string.limit_title_total_list_items, current = 64, free = 100, vip = 5000),
                VipUsageLimit(R.string.limit_title_watchlist_items, current = 120, free = 100, vip = 5000),
                VipUsageLimit(R.string.limit_title_smart_lists, current = 1, free = 2, vip = 20),
                VipUsageLimit(R.string.limit_title_personal_lists, current = 2, free = 2, vip = 2),
            ),
            modifier = Modifier.padding(16.dp),
        )
    }
}
