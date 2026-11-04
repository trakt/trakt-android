package tv.trakt.trakt.core.settings.usecases

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import com.google.firebase.Firebase
import com.google.firebase.remoteconfig.remoteConfig
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import tv.trakt.trakt.common.firebase.FirebaseConfig.RemoteKey.SEASONAL_THEME
import tv.trakt.trakt.common.firebase.analytics.Analytics
import tv.trakt.trakt.ui.theme.model.SeasonalTheme
import java.time.LocalDate

internal class SeasonalThemeUseCase(
    private val dataStore: DataStore<Preferences>,
    private val clearDiscoverCaches: ClearSeasonalDiscoverCachesUseCase,
    private val analytics: Analytics,
) {
    private val remoteConfig = Firebase.remoteConfig

    fun currentSeason(): SeasonalTheme? {
        return SeasonalTheme.NewYear
        return SeasonalTheme.fromRemoteValue(
            remoteConfig.getString(SEASONAL_THEME),
        )
    }

    fun observeActive(): Flow<SeasonalTheme?> {
        return dataStore.data.map { prefs ->
            val season = currentSeason() ?: return@map null
            val enabled = prefs[enabledKey(season)] ?: true
            season.takeIf { enabled }
        }
    }

    fun observePendingSplash(): Flow<SeasonalTheme?> {
        return dataStore.data.map { prefs ->
            val season = currentSeason() ?: return@map null
            val enabled = prefs[enabledKey(season)] ?: true
            val shown = prefs[splashShownKey(season)] ?: false
            season.takeIf { enabled && !shown }
        }
    }

    suspend fun markSplashShown(season: SeasonalTheme) {
        clearDiscoverCaches.clear()
        dataStore.edit { prefs ->
            prefs[splashShownKey(season)] = true
        }
    }

    suspend fun activeSeason(): SeasonalTheme? {
        return observeActive().first()
    }

    suspend fun setEnabled(
        season: SeasonalTheme,
        enabled: Boolean,
    ) {
        if (enabled) {
            clearDiscoverCaches.clear()
        }
        dataStore.edit { prefs ->
            prefs[enabledKey(season)] = enabled
        }
        analytics.logSeasonalFilter(id = season.remoteValue, enabled = enabled)
    }

    private fun splashShownKey(season: SeasonalTheme): Preferences.Key<Boolean> {
        return seasonKey(prefix = "key_seasonal_splash_shown", season = season)
    }

    private fun enabledKey(season: SeasonalTheme): Preferences.Key<Boolean> {
        return seasonKey(prefix = "key_seasonal_theme_enabled", season = season)
    }

    private fun seasonKey(
        prefix: String,
        season: SeasonalTheme,
    ): Preferences.Key<Boolean> {
        val year = season.seasonYear(LocalDate.now())
        return booleanPreferencesKey("${prefix}_${season.remoteValue}_$year")
    }
}
