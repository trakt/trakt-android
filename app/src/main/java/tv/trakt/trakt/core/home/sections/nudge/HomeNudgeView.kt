package tv.trakt.trakt.core.home.sections.nudge

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import tv.trakt.trakt.common.helpers.extensions.PreviewTraktLightDark
import tv.trakt.trakt.common.helpers.extensions.onClick
import tv.trakt.trakt.common.helpers.extensions.rememberNumberFormat
import tv.trakt.trakt.core.home.sections.nudge.model.VipNudge
import tv.trakt.trakt.core.home.sections.nudge.model.VipNudgeLimit
import tv.trakt.trakt.helpers.extensions.TraktThemePreview
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.components.buttons.TertiaryButton
import tv.trakt.trakt.ui.components.vip.onUpsellSeen
import tv.trakt.trakt.ui.theme.DefaultCardShape
import tv.trakt.trakt.ui.theme.TraktTheme

private val IconSize = 22.dp
private val CloseIconSize = 14.dp
private val TouchTargetSize = 48.dp
private val MeterWidth = 152.dp
private val MeterHeight = 4.dp
private val MeterGap = 2.dp

@Composable
internal fun HomeNudgeView(
    nudge: VipNudge,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
    onDismissClick: () -> Unit = {},
    onShown: () -> Unit = {},
    onSeen: () -> Unit = {},
) {
    val currentOnShown by rememberUpdatedState(onShown)
    LaunchedEffect(Unit) {
        currentOnShown()
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .onUpsellSeen(onSeen)
            .background(TraktTheme.colors.panelCardContainer, DefaultCardShape),
    ) {
        Row(
            horizontalArrangement = spacedBy(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    start = 16.dp,
                    top = 14.dp,
                    bottom = 14.dp,
                    end = TouchTargetSize,
                ),
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_crown),
                contentDescription = null,
                tint = TraktTheme.colors.vipAccent,
                modifier = Modifier.size(IconSize),
            )

            Column(
                verticalArrangement = spacedBy(6.dp),
                modifier = Modifier.weight(1F),
            ) {
                when (nudge) {
                    is VipNudge.Features -> FeaturesContent(nudge)
                    is VipNudge.Limit -> LimitContent(nudge)
                }

                TertiaryButton(
                    text = stringResource(R.string.vip_nudge_action),
                    onClick = onClick,
                    modifier = Modifier.padding(top = 4.dp),
                )
            }
        }

        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .size(TouchTargetSize)
                .onClick(onClick = onDismissClick),
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_close),
                contentDescription = null,
                tint = TraktTheme.colors.textSecondary,
                modifier = Modifier.size(CloseIconSize),
            )
        }
    }
}

@Composable
private fun FeaturesContent(nudge: VipNudge.Features) {
    NudgeTitle(stringResource(R.string.vip_nudge_feature_title))
    NudgeDetail(
        stringResource(
            R.string.vip_nudge_feature_body_short,
            rememberNumberFormat(nudge.vipWatchlistItems),
        ),
    )
}

@Composable
private fun LimitContent(nudge: VipNudge.Limit) {
    val used = rememberNumberFormat(nudge.used)
    val total = rememberNumberFormat(nudge.total)
    val vip = rememberNumberFormat(nudge.vip)

    NudgeTitle(stringResource(nudge.titleRes(), used, total))

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = spacedBy(10.dp),
    ) {
        NudgeMeter(nudge)

        Text(
            text = buildAnnotatedString {
                withStyle(SpanStyle(color = TraktTheme.colors.textPrimary, fontWeight = FontWeight.Bold)) {
                    append(used)
                }
                append(" / ")
                append(total)
            },
            style = TraktTheme.typography.paragraphSmaller,
            color = TraktTheme.colors.textSecondary,
            maxLines = 1,
        )
    }

    NudgeDetail(
        when (nudge.limit) {
            VipNudgeLimit.WatchlistItems -> stringResource(R.string.vip_nudge_vip_limit_watchlist_items, vip)
            VipNudgeLimit.TotalListItems -> stringResource(R.string.vip_nudge_vip_limit_list_items, vip)
            VipNudgeLimit.StaticLists -> stringResource(R.string.vip_nudge_vip_limit_lists)
            VipNudgeLimit.TotalNotes -> stringResource(R.string.vip_nudge_vip_limit_notes, vip)
        },
    )
}

private fun VipNudge.Limit.titleRes(): Int {
    return if (isOverLimit) {
        when (limit) {
            VipNudgeLimit.WatchlistItems -> R.string.vip_nudge_over_watchlist_items
            VipNudgeLimit.TotalListItems -> R.string.vip_nudge_over_list_items
            VipNudgeLimit.StaticLists -> R.string.vip_nudge_over_lists
            VipNudgeLimit.TotalNotes -> R.string.vip_nudge_over_notes
        }
    } else {
        when (limit) {
            VipNudgeLimit.WatchlistItems -> R.string.vip_nudge_within_watchlist_items
            VipNudgeLimit.TotalListItems -> R.string.vip_nudge_within_list_items
            VipNudgeLimit.StaticLists -> R.string.vip_nudge_within_lists
            VipNudgeLimit.TotalNotes -> R.string.vip_nudge_within_notes
        }
    }
}

@Composable
private fun NudgeTitle(text: String) {
    Text(
        text = text,
        style = TraktTheme.typography.paragraphSmall.copy(fontWeight = FontWeight.Bold),
        color = TraktTheme.colors.textPrimary,
    )
}

@Composable
private fun NudgeDetail(text: String) {
    Text(
        text = text,
        style = TraktTheme.typography.paragraphSmaller,
        color = TraktTheme.colors.textSecondary,
    )
}

@Composable
private fun NudgeMeter(nudge: VipNudge.Limit) {
    val accent = TraktTheme.colors.vipAccent
    val colors = MeterColors(
        track = accent.copy(alpha = 0.2F),
        base = if (nudge.over > 0) accent.copy(alpha = 0.55F) else accent,
        over = accent,
    )

    Spacer(
        modifier = Modifier
            .width(MeterWidth)
            .height(MeterHeight)
            .drawBehind {
                val mirror = if (layoutDirection == LayoutDirection.Rtl) -1F else 1F
                scale(scaleX = mirror, scaleY = 1F) {
                    drawMeter(nudge, colors)
                }
            },
    )
}

private data class MeterColors(
    val track: Color,
    val base: Color,
    val over: Color,
)

private fun DrawScope.drawMeter(
    nudge: VipNudge.Limit,
    colors: MeterColors,
) {
    val height = size.height
    val radius = CornerRadius(height / 2)

    drawRoundRect(
        color = colors.track,
        size = size,
        cornerRadius = radius,
    )

    val fillWidth = size.width * nudge.fill
    val weight = nudge.base + nudge.over
    if (fillWidth <= 0F || weight <= 0) return

    val gap = if (nudge.base > 0 && nudge.over > 0) MeterGap.toPx() else 0F
    val segmentsWidth = fillWidth - gap
    val baseWidth = segmentsWidth * nudge.base / weight

    if (nudge.base > 0) {
        drawRoundRect(
            color = colors.base,
            size = Size(baseWidth, height),
            cornerRadius = radius,
        )
    }

    if (nudge.over > 0) {
        val overStart = if (nudge.base > 0) baseWidth + gap else 0F
        drawRoundRect(
            color = colors.over,
            topLeft = Offset(overStart, 0F),
            size = Size(fillWidth - overStart, height),
            cornerRadius = radius,
        )
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
            HomeNudgeView(
                nudge = VipNudge.Limit(
                    limit = VipNudgeLimit.WatchlistItems,
                    used = 830,
                    total = 1000,
                    vip = 5000,
                ),
            )
            HomeNudgeView(
                nudge = VipNudge.Limit(
                    limit = VipNudgeLimit.StaticLists,
                    used = 7,
                    total = 5,
                    vip = 50,
                ),
            )
            HomeNudgeView(
                nudge = VipNudge.Features(vipWatchlistItems = 5000),
            )
        }
    }
}
