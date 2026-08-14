package tv.trakt.trakt.ui.theme.model

import androidx.annotation.StringRes
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

    companion object {
        val Default: ThemeMode = Dark
    }
}
