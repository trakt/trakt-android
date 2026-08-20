package tv.trakt.trakt.core.settings.data

import android.content.Context
import androidx.core.content.edit
import tv.trakt.trakt.ui.theme.model.ThemeMode

private const val PREFERENCES_NAME = "theme_mode_cache"
private const val KEY_THEME_MODE = "key_theme_mode"

/**
 * Startup mirror of the DataStore-owned theme mode. DataStore has no synchronous read, but the night
 * mode has to be applied before the first frame, so the value is duplicated into a store that does.
 * DataStore stays the source of truth - this cache is written through on every observed value.
 */
internal class ThemeModeCache(
    context: Context,
) {
    private val preferences = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)

    fun read(): ThemeMode? {
        return preferences.getString(KEY_THEME_MODE, null)
            ?.let { value -> ThemeMode.entries.find { it.name == value } }
    }

    fun write(mode: ThemeMode) {
        preferences.edit { putString(KEY_THEME_MODE, mode.name) }
    }
}
