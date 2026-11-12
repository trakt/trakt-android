package tv.trakt.trakt.ui.theme.model

import androidx.annotation.DrawableRes
import tv.trakt.trakt.common.ui.theme.colors.ChristmasDarkColors
import tv.trakt.trakt.common.ui.theme.colors.ChristmasLightColors
import tv.trakt.trakt.common.ui.theme.colors.DoomsdayDarkColors
import tv.trakt.trakt.common.ui.theme.colors.DoomsdayLightColors
import tv.trakt.trakt.common.ui.theme.colors.HalloweenDarkColors
import tv.trakt.trakt.common.ui.theme.colors.HalloweenLightColors
import tv.trakt.trakt.common.ui.theme.colors.NewYearDarkColors
import tv.trakt.trakt.common.ui.theme.colors.NewYearLightColors
import tv.trakt.trakt.common.ui.theme.colors.TraktColors
import tv.trakt.trakt.resources.R
import java.time.LocalDate
import java.time.Month

internal enum class SeasonalTheme(
    val remoteValue: String,
    @param:DrawableRes val icon: Int,
    @param:DrawableRes val streakFlameMedium: Int,
    @param:DrawableRes val streakFlameLarge: Int,
    val darkColors: TraktColors,
    val lightColors: TraktColors,
    val startMonth: Month,
    val hasSplash: Boolean = true,
) {
    Halloween(
        remoteValue = "halloween",
        icon = R.drawable.ic_pumpkin,
        streakFlameMedium = R.drawable.ic_flame_2_halloween,
        streakFlameLarge = R.drawable.ic_flame_3_halloween,
        darkColors = HalloweenDarkColors,
        lightColors = HalloweenLightColors,
        startMonth = Month.OCTOBER,
    ),
    Christmas(
        remoteValue = "christmas",
        icon = R.drawable.ic_christmas_tree,
        streakFlameMedium = R.drawable.ic_flame_2_christmas,
        streakFlameLarge = R.drawable.ic_flame_3_christmas,
        darkColors = ChristmasDarkColors,
        lightColors = ChristmasLightColors,
        startMonth = Month.DECEMBER,
    ),
    NewYear(
        remoteValue = "new-year",
        icon = R.drawable.ic_sparkles,
        streakFlameMedium = R.drawable.ic_flame_2_new_year,
        streakFlameLarge = R.drawable.ic_flame_3_new_year,
        darkColors = NewYearDarkColors,
        lightColors = NewYearLightColors,
        startMonth = Month.DECEMBER,
    ),
    Doomsday(
        remoteValue = "doomsday",
        icon = R.drawable.ic_avengers,
        streakFlameMedium = R.drawable.ic_flame_2_doomsday,
        streakFlameLarge = R.drawable.ic_flame_3_doomsday,
        darkColors = DoomsdayDarkColors,
        lightColors = DoomsdayLightColors,
        startMonth = Month.NOVEMBER,
        hasSplash = false,
    ),
    ;

    /**
     * The year this season started in. A season that runs into January, such as New Year,
     * keeps the year of its start so it is treated as one season across the turn of the year.
     */
    fun seasonYear(date: LocalDate): Int {
        return when {
            date.month < startMonth -> date.year - 1
            else -> date.year
        }
    }

    companion object {
        fun fromRemoteValue(value: String?): SeasonalTheme? {
            val normalized = value?.trim()?.lowercase()
            if (normalized.isNullOrEmpty()) return null

            return entries.firstOrNull { it.remoteValue == normalized }
        }
    }
}
