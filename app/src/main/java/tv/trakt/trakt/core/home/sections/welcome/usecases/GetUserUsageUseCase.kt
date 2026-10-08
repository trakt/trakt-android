package tv.trakt.trakt.core.home.sections.welcome.usecases

import tv.trakt.trakt.common.auth.session.SessionManager
import tv.trakt.trakt.common.networking.api.v3.V3Api
import tv.trakt.trakt.common.networking.api.v3.model.V3UsageResponse

internal class GetUserUsageUseCase(
    private val v3Api: V3Api,
    private val sessionManager: SessionManager,
) {
    suspend fun getUserUsage(): V3UsageResponse? {
        if (!sessionManager.isAuthenticated()) {
            return null
        }
        return v3Api.getUsage()
    }
}
