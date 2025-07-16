package tv.trakt.app.tv.auth.session

import kotlinx.coroutines.flow.Flow
import tv.trakt.app.tv.common.model.User

internal interface SessionManager {
    suspend fun saveProfile(user: User)

    suspend fun getProfile(): User?

    suspend fun observeProfile(): Flow<User?>

    suspend fun isAuthenticated(): Boolean

    suspend fun clear()
}
