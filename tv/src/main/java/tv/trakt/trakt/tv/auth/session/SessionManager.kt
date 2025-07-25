package tv.trakt.trakt.tv.auth.session

import kotlinx.coroutines.flow.Flow
import tv.trakt.trakt.common.model.User

internal interface SessionManager {
    suspend fun saveProfile(user: User)

    suspend fun getProfile(): User?

    suspend fun observeProfile(): Flow<User?>

    suspend fun isAuthenticated(): Boolean

    suspend fun clear()
}
