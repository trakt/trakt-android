package tv.trakt.trakt.widgets.ui

import android.annotation.SuppressLint
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.sp
import androidx.glance.text.FontWeight
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import tv.trakt.trakt.common.ui.theme.colors.DarkColors
import tv.trakt.trakt.common.ui.theme.colors.Green600
import tv.trakt.trakt.common.ui.theme.colors.LightColors
import tv.trakt.trakt.common.ui.theme.colors.Purple400
import tv.trakt.trakt.common.ui.theme.colors.Purple600
import tv.trakt.trakt.common.ui.theme.colors.Red500
import tv.trakt.trakt.common.ui.theme.colors.Shade930
import tv.trakt.trakt.common.ui.theme.colors.TraktColors
import tv.trakt.trakt.ui.theme.model.ThemeMode
import androidx.glance.color.ColorProvider as DayNightColorProvider

private const val SEMI_TRANSPARENT_ALPHA = 0.4F

private val DefaultWidgetColors = widgetColors(ThemeMode.Default)

private val LocalWidgetColors = staticCompositionLocalOf { DefaultWidgetColors }
private val LocalWidgetTextStyles = staticCompositionLocalOf { WidgetTextStyles(DefaultWidgetColors) }

internal fun widgetColors(theme: ThemeMode): WidgetColors {
    return when (theme) {
        ThemeMode.System -> WidgetColors(day = LightColors, night = DarkColors)
        ThemeMode.Light -> WidgetColors(day = LightColors, night = LightColors)
        ThemeMode.Dark -> WidgetColors(day = DarkColors, night = DarkColors)
    }
}

@SuppressLint("RestrictedApi")
internal class WidgetColors(
    private val day: TraktColors,
    private val night: TraktColors,
) {
    val backgroundPrimary = token { it.backgroundPrimary }
    val backgroundTranslucent = token { it.backgroundPrimary.copy(alpha = SEMI_TRANSPARENT_ALPHA) }
    val backgroundNone = ColorProvider(Color.Transparent)
    val textPrimary = token { it.textPrimary }
    val textPrimaryOnAccent = token { it.textPrimaryOnAccent }
    val textSecondary = token { it.textSecondary }
    val accent = token { it.accent }
    val placeholderContainer = token { it.placeholderContainer }
    val chipContainer = token { it.chipContainerOnContent }
    val chipContent = token { it.chipContent }
    val chipProgressTrack = dayNightTuple(day = Shade930, night = Shade930)

    val todayMarker = dayNightTuple(day = Purple600, night = Purple400)

    val premiereDot = ColorProvider(Green600)
    val finaleDot = ColorProvider(Red500)

    val streakPillActive = token { it.streakLevel3 }
    val streakPillMissed = token { it.streakTileEmpty }
    val streakTodayRing = token { it.streakTileToday }

    private fun token(select: (TraktColors) -> Color): ColorProvider {
        return DayNightColorProvider(day = select(day), night = select(night))
    }

    private fun dayNightTuple(
        day: Color,
        night: Color,
    ): ColorProvider {
        return DayNightColorProvider(day = day, night = night)
    }
}

internal class WidgetTextStyles(
    colors: WidgetColors,
) {
    val heading = TextStyle(
        color = colors.textPrimary,
        fontSize = 18.sp,
        fontWeight = FontWeight.Bold,
    )

    val headingCompact = TextStyle(
        color = colors.textPrimary,
        fontSize = 13.sp,
        fontWeight = FontWeight.Bold,
    )

    val dayHeading = TextStyle(
        color = colors.textPrimary,
        fontSize = 16.sp,
        fontWeight = FontWeight.Bold,
    )

    val cardTitle = TextStyle(
        color = colors.textPrimary,
        fontSize = 12.sp,
        fontWeight = FontWeight.Medium,
    )

    val cardSubtitle = TextStyle(
        color = colors.textSecondary,
        fontSize = 11.sp,
        fontWeight = FontWeight.Normal,
    )

    val meta = TextStyle(
        color = colors.textPrimaryOnAccent,
        fontSize = 10.sp,
        fontWeight = FontWeight.Bold,
    )

    val message = TextStyle(
        color = colors.textSecondary,
        fontSize = 12.sp,
        fontWeight = FontWeight.Normal,
    )
}

@Composable
internal fun WidgetTheme(
    theme: ThemeMode,
    content: @Composable () -> Unit,
) {
    val colors = remember(theme) { widgetColors(theme) }
    val textStyles = remember(colors) { WidgetTextStyles(colors) }

    CompositionLocalProvider(
        LocalWidgetColors provides colors,
        LocalWidgetTextStyles provides textStyles,
        content = content,
    )
}

internal object WidgetTheme {
    val colors: WidgetColors
        @Composable @ReadOnlyComposable
        get() = LocalWidgetColors.current

    val textStyles: WidgetTextStyles
        @Composable @ReadOnlyComposable
        get() = LocalWidgetTextStyles.current
}
