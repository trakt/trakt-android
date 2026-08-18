package tv.trakt.trakt.ui.theme.model

import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
import androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_NO
import androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_YES
import tv.trakt.trakt.resources.R

internal enum class ThemeMode {
    System,
    Light,
    Dark,
    ;

    @StringRes
    fun displayName(): Int {
        return when (this) {
            System -> R.string.option_text_theme_system
            Light -> R.string.option_text_theme_light
            Dark -> R.string.option_text_theme_dark
        }
    }

    fun toNightMode(): Int {
        return when (this) {
            System -> MODE_NIGHT_FOLLOW_SYSTEM
            Light -> MODE_NIGHT_NO
            Dark -> MODE_NIGHT_YES
        }
    }

    companion object {
        val Default: ThemeMode = Dark

        fun fromNightMode(nightMode: Int): ThemeMode {
            return when (nightMode) {
                MODE_NIGHT_NO -> Light
                MODE_NIGHT_YES -> Dark
                MODE_NIGHT_FOLLOW_SYSTEM -> System
                else -> Default
            }
        }

        fun current(): ThemeMode = fromNightMode(AppCompatDelegate.getDefaultNightMode())
    }
}
