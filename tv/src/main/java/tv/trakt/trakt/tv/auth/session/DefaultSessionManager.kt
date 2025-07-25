package tv.trakt.trakt.tv.auth.session

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.Json
import tv.trakt.trakt.common.auth.TokenProvider
import tv.trakt.trakt.tv.common.model.User

private val KEY_USER_PROFILE = stringPreferencesKey("key_user_profile")

internal class DefaultSessionManager(
    private val tokenProvider: TokenProvider,
    private val dataStore: DataStore<Preferences>,
) : SessionManager {
    override suspend fun isAuthenticated(): Boolean {
        return tokenProvider.getToken() != null
    }

    override suspend fun clear() {
        tokenProvider.clear()
        dataStore.edit { it.clear() }
    }

    override suspend fun saveProfile(user: User) {
        val userData = Json.encodeToString(user)
        dataStore.edit {
            it[KEY_USER_PROFILE] = userData
        }
    }

    override suspend fun getProfile(): User? {
        val userData = dataStore.data.first()[KEY_USER_PROFILE] ?: return null
        val decodedUserData = runCatching {
            Json.decodeFromString<User?>(userData)
        }
        return decodedUserData.getOrElse {
            clear()
            null
        }
    }

    override suspend fun observeProfile(): Flow<User?> {
        return dataStore.data.map { preferences ->
            val data = preferences[KEY_USER_PROFILE]
            if (data.isNullOrEmpty()) {
                return@map null
            }
            return@map runCatching {
                Json.decodeFromString<User>(data)
            }.getOrElse {
                clear()
                null
            }
        }
    }
}
