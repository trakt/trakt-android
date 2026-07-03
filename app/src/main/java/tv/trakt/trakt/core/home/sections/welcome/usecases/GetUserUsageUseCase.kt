package tv.trakt.trakt.core.home.sections.welcome.usecases

import tv.trakt.trakt.common.networking.api.v3.V3Api
import tv.trakt.trakt.common.networking.api.v3.model.V3UsageResponse

internal class GetUserUsageUseCase(
    private val v3Api: V3Api,
) {
    suspend fun getUserUsage(): V3UsageResponse {
        return v3Api.getUsage()
    }
}
