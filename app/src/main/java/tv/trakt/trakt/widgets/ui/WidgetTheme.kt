package tv.trakt.trakt.widgets.ui

import android.annotation.SuppressLint
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.sp
import androidx.glance.text.FontWeight
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import tv.trakt.trakt.common.ui.theme.colors.DarkColors
import tv.trakt.trakt.common.ui.theme.colors.Green600
import tv.trakt.trakt.common.ui.theme.colors.Purple400
import tv.trakt.trakt.common.ui.theme.colors.Purple500
import tv.trakt.trakt.common.ui.theme.colors.Red500
import tv.trakt.trakt.common.ui.theme.colors.Shade700
import tv.trakt.trakt.common.ui.theme.colors.Shade930

private const val SEMI_TRANSPARENT_ALPHA = 0.4F

@SuppressLint("RestrictedApi")
internal object WidgetColors {
    val backgroundPrimary = ColorProvider(DarkColors.backgroundPrimary)
    val backgroundTranslucent = ColorProvider(
        DarkColors.backgroundPrimary.copy(alpha = SEMI_TRANSPARENT_ALPHA),
    )
    val backgroundNone = ColorProvider(Color.Transparent)
    val textPrimary = ColorProvider(DarkColors.textPrimary)
    val textSecondary = ColorProvider(DarkColors.textSecondary)
    val accent = ColorProvider(DarkColors.accent)
    val placeholderContainer = ColorProvider(DarkColors.placeholderContainer)
    val chipContainer = ColorProvider(DarkColors.chipContainerOnContent)
    val chipContent = ColorProvider(DarkColors.chipContent)
    val chipProgressTrack = ColorProvider(Shade930)

    val todayMarker = ColorProvider(Purple400)

    val premiereDot = ColorProvider(Green600)
    val finaleDot = ColorProvider(Red500)

    val streakPillActive = ColorProvider(Purple500)
    val streakPillMissed = ColorProvider(Shade700)
    val streakTodayRing = ColorProvider(Color.White)
}

internal object WidgetTextStyles {
    val heading = TextStyle(
        color = WidgetColors.textPrimary,
        fontSize = 18.sp,
        fontWeight = FontWeight.Bold,
    )

    val headingCompact = TextStyle(
        color = WidgetColors.textPrimary,
        fontSize = 13.sp,
        fontWeight = FontWeight.Bold,
    )

    val dayHeading = TextStyle(
        color = WidgetColors.textPrimary,
        fontSize = 16.sp,
        fontWeight = FontWeight.Bold,
    )

    val cardTitle = TextStyle(
        color = WidgetColors.textPrimary,
        fontSize = 12.sp,
        fontWeight = FontWeight.Medium,
    )

    val cardSubtitle = TextStyle(
        color = WidgetColors.textSecondary,
        fontSize = 11.sp,
        fontWeight = FontWeight.Normal,
    )

    val meta = TextStyle(
        color = WidgetColors.textPrimary,
        fontSize = 10.sp,
        fontWeight = FontWeight.Bold,
    )

    val message = TextStyle(
        color = WidgetColors.textSecondary,
        fontSize = 12.sp,
        fontWeight = FontWeight.Normal,
    )
}
