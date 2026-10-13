package tv.trakt.trakt.ui.theme.model

import androidx.annotation.DrawableRes
import tv.trakt.trakt.common.ui.theme.colors.HalloweenDarkColors
import tv.trakt.trakt.common.ui.theme.colors.HalloweenLightColors
import tv.trakt.trakt.common.ui.theme.colors.TraktColors
import tv.trakt.trakt.resources.R

internal enum class SeasonalTheme(
    val remoteValue: String,
    @param:DrawableRes val icon: Int,
    @param:DrawableRes val streakFlameMedium: Int,
    @param:DrawableRes val streakFlameLarge: Int,
    val darkColors: TraktColors,
    val lightColors: TraktColors,
) {
    Halloween(
        remoteValue = "halloween",
        icon = R.drawable.ic_pumpkin,
        streakFlameMedium = R.drawable.ic_flame_2_halloween,
        streakFlameLarge = R.drawable.ic_flame_3_halloween,
        darkColors = HalloweenDarkColors,
        lightColors = HalloweenLightColors,
    ),
    ;

    companion object {
        fun fromRemoteValue(value: String?): SeasonalTheme? {
            val normalized = value?.trim()?.lowercase()
            if (normalized.isNullOrEmpty()) return null

            return entries.firstOrNull { it.remoteValue == normalized }
        }
    }
}
