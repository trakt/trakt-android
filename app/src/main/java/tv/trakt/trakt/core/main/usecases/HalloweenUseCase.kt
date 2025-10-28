package tv.trakt.trakt.core.main.usecases

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

/**
 * Preference key to store whether the user has enabled or disabled the Halloween theme for 2025.
 * Default is true (enabled).
 */
internal val KEY_HALLOWEEN_USER_ENABLED = booleanPreferencesKey("key_halloween_user_disabled_2025")

/**
 * Use case to manage the Halloween theme preference.
 */
internal class HalloweenUseCase(
    private val mainDataStore: DataStore<Preferences>,
) {
    suspend fun toggleHalloween(enabled: Boolean) {
        mainDataStore.edit { prefs ->
            prefs[KEY_HALLOWEEN_USER_ENABLED] = enabled
        }
    }

    suspend fun isHalloweenEnabled(): Boolean {
        val prefs = mainDataStore.data.first()
        return prefs[KEY_HALLOWEEN_USER_ENABLED] ?: true
    }

    fun observeHalloweenEnabled(): Flow<Boolean> {
        return mainDataStore.data.map { prefs ->
            prefs[KEY_HALLOWEEN_USER_ENABLED] ?: true
        }
    }
}
