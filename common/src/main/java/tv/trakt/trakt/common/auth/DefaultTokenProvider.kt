package tv.trakt.trakt.common.auth

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.first
import timber.log.Timber
import tv.trakt.trakt.common.auth.model.TraktAccessToken

private val KEY_ACCESS_TOKEN = stringPreferencesKey("key_access_token")
private val KEY_REFRESH_TOKEN = stringPreferencesKey("key_refresh_token")
private val KEY_EXPIRES_IN = longPreferencesKey("key_expires_in")
private val KEY_CREATED_AT = longPreferencesKey("key_created_at")

internal class DefaultTokenProvider(
    private val dataStore: DataStore<Preferences>,
) : TokenProvider {
    override suspend fun saveToken(token: TraktAccessToken) {
        dataStore.edit {
            it[KEY_ACCESS_TOKEN] = token.accessToken
            it[KEY_REFRESH_TOKEN] = token.refreshToken
            it[KEY_EXPIRES_IN] = token.expiresIn
            it[KEY_CREATED_AT] = token.createdAt
        }
        Timber.d("Token stored!")
    }

    override suspend fun getToken(): TraktAccessToken? {
        val data = dataStore.data.first()

        val accessToken = data[KEY_ACCESS_TOKEN]
        val refreshToken = data[KEY_REFRESH_TOKEN]
        val expiresIn = data[KEY_EXPIRES_IN] ?: 0
        val createdAt = data[KEY_CREATED_AT] ?: 0

        if (accessToken != null && refreshToken != null) {
            return TraktAccessToken(
                accessToken = accessToken,
                refreshToken = refreshToken,
                expiresIn = expiresIn,
                createdAt = createdAt,
            )
        }

        Timber.d("Token not found.")
        return null
    }

    override suspend fun clear() {
        dataStore.edit {
            it.clear()
        }
        Timber.d("Token provider cleared.")
    }
}
