package tv.trakt.trakt.core.billing.ui

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import tv.trakt.trakt.common.helpers.extensions.rememberNumberFormat
import tv.trakt.trakt.common.ui.composables.FilmProgressIndicator
import tv.trakt.trakt.common.ui.theme.colors.Red400
import tv.trakt.trakt.common.ui.theme.colors.Red500
import tv.trakt.trakt.core.billing.model.VipUsageLimit
import tv.trakt.trakt.helpers.extensions.TraktThemeLightDark
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.components.chips.FilterChip
import tv.trakt.trakt.ui.theme.TraktTheme

private const val COLLAPSED_ITEMS = 3
private const val FREE_ZONE_FRACTION = 0.3F

private val BarHeight = 8.dp
private val BarGap = 4.dp
private val StopIndicatorSize = 4.dp
private val TouchTargetSize = 48.dp
private val LoadingIndicatorSize = 32.dp

private val PlaceholderLimits = persistentListOf(
    VipUsageLimit(R.string.limit_title_total_list_items, current = 0, free = 1, vip = 2),
    VipUsageLimit(R.string.limit_title_watchlist_items, current = 0, free = 1, vip = 2),
    VipUsageLimit(R.string.limit_title_smart_lists, current = 0, free = 1, vip = 2),
    VipUsageLimit(R.string.limit_title_personal_lists, current = 0, free = 1, vip = 2),
    VipUsageLimit(R.string.limit_title_digital_library, current = 0, free = 1, vip = 2),
)

@Composable
internal fun VipUsageLimitsView(
    limits: ImmutableList<VipUsageLimit>?,
    loading: Boolean,
    modifier: Modifier = Modifier,
) {
    var expanded by rememberSaveable { mutableStateOf(false) }
    val contentAlpha by animateFloatAsState(
        targetValue = if (loading) 0F else 1F,
        label = "contentAlpha",
    )

    val displayLimits = if (loading) PlaceholderLimits else limits ?: persistentListOf()
    val visibleLimits = if (expanded) displayLimits else displayLimits.take(COLLAPSED_ITEMS)

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

        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column(
                verticalArrangement = spacedBy(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .graphicsLayer { alpha = contentAlpha },
            ) {
                visibleLimits.forEach { limit ->
                    VipUsageLimitItem(limit = limit)
                }

                if (displayLimits.size > COLLAPSED_ITEMS && !expanded) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally)
                            .heightIn(min = TouchTargetSize)
                            .padding(horizontal = 16.dp),
                    ) {
                        FilterChip(
                            selected = false,
                            text = stringResource(R.string.button_label_see_more_limits),
                            height = 36.dp,
                            paddingHorizontal = PaddingValues(
                                start = 12.dp,
                                end = 16.dp,
                            ),
                            onClick = {
                                if (!loading) expanded = true
                            },
                        )
                    }
                }
            }

            if (loading) {
                FilmProgressIndicator(size = LoadingIndicatorSize)
            }
        }
    }
}

@Composable
private fun VipUsageLimitItem(limit: VipUsageLimit) {
    val isOverLimit = limit.current > limit.free
    val hasVipZone = limit.vip != limit.free

    Column(
        verticalArrangement = spacedBy(8.dp),
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
                color = if (isOverLimit) Red400 else TraktTheme.colors.textSecondary,
            )

            Spacer(modifier = Modifier.width(12.dp))

            if (hasVipZone) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = spacedBy(2.dp),
                ) {
                    Text(
                        text = stringResource(R.string.tag_text_vip),
                        style = TraktTheme.typography.meta,
                        color = TraktTheme.colors.textPrimary,
                    )
                    Icon(
                        painter = painterResource(R.drawable.ic_back_arrow),
                        contentDescription = null,
                        tint = TraktTheme.colors.textPrimary,
                        modifier = Modifier
                            .rotate(180F)
                            .size(12.dp),
                    )
                    Text(
                        text = rememberNumberFormat(limit.vip),
                        style = TraktTheme.typography.meta,
                        color = TraktTheme.colors.textPrimary,
                    )
                }
            }
        }

        VipUsageBar(
            limit = limit,
            hasVipZone = hasVipZone,
            isOverLimit = isOverLimit,
        )
    }
}

@Composable
private fun VipUsageBar(
    limit: VipUsageLimit,
    hasVipZone: Boolean,
    isOverLimit: Boolean,
) {
    val accent = TraktTheme.colors.vipAccent
    val colors = UsageBarColors(
        indicator = if (isOverLimit) Red500 else accent,
        track = accent.copy(alpha = 0.32F),
        vipTrack = accent.copy(alpha = 0.12F),
        stop = accent,
    )

    val zoneFraction = if (hasVipZone) FREE_ZONE_FRACTION else 1F
    val progress = when {
        limit.free <= 0 -> 0F
        else -> (limit.current.toFloat() / limit.free).coerceIn(0F, 1F)
    }

    Spacer(
        modifier = Modifier
            .fillMaxWidth()
            .height(BarHeight)
            .drawBehind {
                val mirror = if (layoutDirection == LayoutDirection.Rtl) -1F else 1F
                scale(scaleX = mirror, scaleY = 1F) {
                    drawUsageBar(
                        zoneFraction = zoneFraction,
                        progress = progress,
                        hasVipZone = hasVipZone,
                        colors = colors,
                    )
                }
            },
    )
}

private data class UsageBarColors(
    val indicator: Color,
    val track: Color,
    val vipTrack: Color,
    val stop: Color,
)

private fun DrawScope.drawUsageBar(
    zoneFraction: Float,
    progress: Float,
    hasVipZone: Boolean,
    colors: UsageBarColors,
) {
    val height = size.height
    val radius = CornerRadius(height / 2)
    val gap = BarGap.toPx()
    val zoneEnd = size.width * zoneFraction

    val indicatorEnd = if (progress > 0F) (zoneEnd * progress).coerceAtLeast(height) else 0F
    if (indicatorEnd > 0F) {
        drawRoundRect(
            color = colors.indicator,
            size = Size(indicatorEnd, height),
            cornerRadius = radius,
        )
    }

    val trackStart = if (indicatorEnd > 0F) indicatorEnd + gap else 0F
    if (progress < 1F && zoneEnd - trackStart > 0F) {
        drawRoundRect(
            color = colors.track,
            topLeft = Offset(trackStart, 0F),
            size = Size(zoneEnd - trackStart, height),
            cornerRadius = radius,
        )
        drawCircle(
            color = colors.stop,
            radius = StopIndicatorSize.toPx() / 2,
            center = Offset(zoneEnd - height / 2, height / 2),
        )
    }

    if (hasVipZone) {
        val vipStart = zoneEnd + gap
        drawRoundRect(
            color = colors.vipTrack,
            topLeft = Offset(vipStart, 0F),
            size = Size(size.width - vipStart, height),
            cornerRadius = radius,
        )
    }
}

@Preview(
    widthDp = 380,
    showBackground = true,
    backgroundColor = 0xFF131517,
)
@Composable
private fun Preview() {
    TraktThemeLightDark {
        VipUsageLimitsView(
            limits = persistentListOf(
                VipUsageLimit(R.string.limit_title_total_list_items, current = 64, free = 100, vip = 5000),
                VipUsageLimit(R.string.limit_title_watchlist_items, current = 120, free = 100, vip = 5000),
                VipUsageLimit(R.string.limit_title_smart_lists, current = 1, free = 2, vip = 20),
                VipUsageLimit(R.string.limit_title_personal_lists, current = 2, free = 2, vip = 2),
            ),
            loading = false,
            modifier = Modifier.padding(16.dp),
        )
    }
}

@Preview(
    widthDp = 380,
    showBackground = true,
    backgroundColor = 0xFF131517,
)
@Composable
private fun LoadingPreview() {
    TraktThemeLightDark {
        VipUsageLimitsView(
            limits = null,
            loading = true,
            modifier = Modifier.padding(16.dp),
        )
    }
}
