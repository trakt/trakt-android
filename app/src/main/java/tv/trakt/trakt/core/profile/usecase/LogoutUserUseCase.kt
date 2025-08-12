package tv.trakt.trakt.core.profile.usecase

import tv.trakt.trakt.common.auth.session.SessionManager

internal class LogoutUserUseCase(
    private val sessionManager: SessionManager,
) {
    suspend fun logoutUser() {
        sessionManager.clear()
    }
}
