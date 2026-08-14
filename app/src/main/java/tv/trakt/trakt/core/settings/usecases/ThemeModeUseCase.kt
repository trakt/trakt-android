package tv.trakt.trakt.core.settings.usecases

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import tv.trakt.trakt.ui.theme.model.ThemeMode

private val KEY_THEME_MODE = stringPreferencesKey("key_theme_mode")

internal class ThemeModeUseCase(
    private val dataStore: DataStore<Preferences>,
) {
    fun observeThemeMode(): Flow<ThemeMode> =
        dataStore.data.map { prefs ->
            prefs[KEY_THEME_MODE]
                ?.let { value -> ThemeMode.entries.find { it.name == value } }
                ?: ThemeMode.Default
        }

    suspend fun getThemeMode(): ThemeMode = observeThemeMode().first()

    suspend fun setThemeMode(mode: ThemeMode) {
        dataStore.edit { prefs ->
            prefs[KEY_THEME_MODE] = mode.name
        }
    }
}
