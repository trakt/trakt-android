package tv.trakt.trakt.core.settings.usecases

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val KEY_NOTIFICATION_ENABLED = booleanPreferencesKey("key_notifications_enabled")

internal class EnableNotificationsUseCase(
    private val dataStore: DataStore<Preferences>,
) {
    suspend fun enableNotifications(enabled: Boolean): Boolean {
        dataStore.updateData {
            it.toMutablePreferences().apply {
                this[KEY_NOTIFICATION_ENABLED] = enabled
            }
        }

        return enabled
    }

    suspend fun isNotificationsEnabled(): Boolean {
        val preferences = dataStore.data.map { prefs ->
            prefs[KEY_NOTIFICATION_ENABLED] ?: false
        }
        return preferences.first()
    }
}
