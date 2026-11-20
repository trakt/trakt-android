package tv.trakt.trakt.common.data.local

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.first
import tv.trakt.trakt.common.model.PortraitToken
import java.time.Instant

private val KEY_TOKEN = stringPreferencesKey("key_portrait_token")
private val KEY_EXPIRES_AT = longPreferencesKey("key_portrait_token_expires_at")

class PortraitTokenLocalSource(
    private val dataStore: DataStore<Preferences>,
) {
    suspend fun getToken(): PortraitToken? {
        val preferences = dataStore.data.first()
        val token = preferences[KEY_TOKEN] ?: return null
        val expiresAt = preferences[KEY_EXPIRES_AT] ?: return null

        return PortraitToken(
            token = token,
            expiresAt = Instant.ofEpochMilli(expiresAt),
        )
    }

    suspend fun setToken(token: PortraitToken) {
        dataStore.edit {
            it[KEY_TOKEN] = token.token
            it[KEY_EXPIRES_AT] = token.expiresAt.toEpochMilli()
        }
    }
}
