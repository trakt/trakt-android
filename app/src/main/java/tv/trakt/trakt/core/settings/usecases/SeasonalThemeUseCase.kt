package tv.trakt.trakt.core.settings.usecases

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import com.google.firebase.Firebase
import com.google.firebase.remoteconfig.remoteConfig
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import tv.trakt.trakt.common.firebase.FirebaseConfig.RemoteKey.SEASONAL_THEME
import tv.trakt.trakt.ui.theme.model.SeasonalTheme

internal class SeasonalThemeUseCase(
    private val dataStore: DataStore<Preferences>,
) {
    private val remoteConfig = Firebase.remoteConfig

    fun currentSeason(): SeasonalTheme? {
        return SeasonalTheme.fromRemoteValue(
            remoteConfig.getString(SEASONAL_THEME),
        )
    }

    fun observeEnabled(season: SeasonalTheme): Flow<Boolean> {
        return dataStore.data.map { prefs ->
            prefs[enabledKey(season)] ?: true
        }
    }

    fun observeActive(): Flow<SeasonalTheme?> {
        return dataStore.data.map { prefs ->
            val season = currentSeason() ?: return@map null
            val enabled = prefs[enabledKey(season)] ?: true
            season.takeIf { enabled }
        }
    }

    suspend fun setEnabled(
        season: SeasonalTheme,
        enabled: Boolean,
    ) {
        dataStore.edit { prefs ->
            prefs[enabledKey(season)] = enabled
        }
    }

    private fun enabledKey(season: SeasonalTheme): Preferences.Key<Boolean> {
        return booleanPreferencesKey("key_seasonal_theme_enabled_${season.remoteValue}")
    }
}
