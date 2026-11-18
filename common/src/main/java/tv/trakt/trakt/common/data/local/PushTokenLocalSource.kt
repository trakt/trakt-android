package tv.trakt.trakt.common.data.local

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.first
import java.util.UUID

private val KEY_DEVICE_ID = stringPreferencesKey("key_push_device_id")
private val KEY_UPLOADED_TOKEN = stringPreferencesKey("key_push_uploaded_token")

class PushTokenLocalSource(
    private val dataStore: DataStore<Preferences>,
) {
    suspend fun getDeviceId(): String {
        val preferences = dataStore.edit { preferences ->
            if (preferences[KEY_DEVICE_ID] == null) {
                preferences[KEY_DEVICE_ID] = UUID.randomUUID().toString()
            }
        }
        return checkNotNull(preferences[KEY_DEVICE_ID])
    }

    suspend fun getUploadedToken(): String? {
        return dataStore.data.first()[KEY_UPLOADED_TOKEN]
    }

    suspend fun setUploadedToken(token: String) {
        dataStore.edit { it[KEY_UPLOADED_TOKEN] = token }
    }

    suspend fun clearUploadedToken() {
        dataStore.edit { it.remove(KEY_UPLOADED_TOKEN) }
    }
}
