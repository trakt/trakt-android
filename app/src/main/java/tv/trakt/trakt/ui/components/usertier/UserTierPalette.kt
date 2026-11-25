package tv.trakt.trakt.ui.components.usertier

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import tv.trakt.trakt.common.model.UserTier

private val VipBase = Color(0xFF6A45E6)
private val VipSheen = Color(0xFFB07BFF)
private val ProBase = Color(0xFFC2287F)
private val ProSheen = Color(0xFFFF6FB5)
private val VeteranBase = Color(0xFF0D8A6C)
private val VeteranSheen = Color(0xFF3FD1A0)
private val EliteBase = Color(0xFF1F5FD8)
private val EliteSheen = Color(0xFF4FB4FF)
private val LegendBase = Color(0xFFA97800)
private val LegendSheen = Color(0xFFFFD54A)

private val TagInk = Color(0xFFFCFBFE)
private val TagShade = Color(0xFF211E25)
private val LegendTagInk = Color(0xFF2B1D00)
private val LegendTagLight = Color(0xFFFFF085)
private val LegendTagDeep = Color(0xFFD08700)

private val LegendRingLight = listOf(
    0F to Color(0xFF8A6000),
    70F / 360F to Color(0xFFA97800),
    150F / 360F to Color(0xFFD9A21C),
    175F / 360F to Color(0xFFE8B83A),
    210F / 360F to Color(0xFFC08A00),
    300F / 360F to Color(0xFF8A6000),
    1F to Color(0xFF8A6000),
)

private val LegendRingDark = listOf(
    0F to Color(0xFF8A6000),
    70F / 360F to Color(0xFFB88600),
    150F / 360F to Color(0xFFFFE27A),
    175F / 360F to Color(0xFFFFF6CC),
    210F / 360F to Color(0xFFE0A800),
    300F / 360F to Color(0xFF8A6000),
    1F to Color(0xFF8A6000),
)

private val FreeRingLight = Color(0xFF0F0A14).copy(alpha = 0.14F)
private val FreeRingDark = TagInk.copy(alpha = 0.12F)

internal fun UserTier.baseColor(): Color {
    return when (this) {
        UserTier.Free -> Color.Transparent
        UserTier.Vip -> VipBase
        UserTier.Pro -> ProBase
        UserTier.Veteran -> VeteranBase
        UserTier.Elite -> EliteBase
        UserTier.Legend -> LegendBase
    }
}

internal fun UserTier.sheenColor(): Color {
    return when (this) {
        UserTier.Free -> Color.Transparent
        UserTier.Vip -> VipSheen
        UserTier.Pro -> ProSheen
        UserTier.Veteran -> VeteranSheen
        UserTier.Elite -> EliteSheen
        UserTier.Legend -> LegendSheen
    }
}

internal fun userTierFreeRing(isLight: Boolean): Color {
    return if (isLight) FreeRingLight else FreeRingDark
}

internal fun UserTier.ringStops(isLight: Boolean): List<Pair<Float, Color>> {
    if (this == UserTier.Legend) {
        return if (isLight) LegendRingLight else LegendRingDark
    }

    val base = baseColor()
    val sheen = if (isLight) lerp(base, sheenColor(), 0.5F) else sheenColor()
    return listOf(
        0F to base,
        120F / 360F to base,
        215F / 360F to sheen,
        290F / 360F to base,
        1F to base,
    )
}

internal fun UserTier.tagBrush(): Brush {
    if (this == UserTier.Legend) {
        return Brush.linearGradient(
            0F to lerp(LegendTagLight, LegendBase, 0.15F),
            1F to lerp(LegendTagDeep, LegendBase, 0.65F),
        )
    }

    return Brush.linearGradient(
        0F to sheenColor(),
        1F to lerp(TagShade, baseColor(), 0.82F),
    )
}

internal fun UserTier.tagInk(): Color {
    return if (this == UserTier.Legend) LegendTagInk else TagInk
}
