package tv.trakt.trakt.core.billing.usecases

import kotlinx.collections.immutable.ImmutableList
import tv.trakt.trakt.common.auth.session.SessionManager
import tv.trakt.trakt.common.networking.api.v3.V3Api
import tv.trakt.trakt.core.billing.model.VipUsageLimit

internal class GetVipUsageLimitsUseCase(
    private val v3Api: V3Api,
    private val sessionManager: SessionManager,
) {
    suspend fun getLimits(): ImmutableList<VipUsageLimit>? {
        if (!sessionManager.isAuthenticated()) {
            return null
        }
        return VipUsageLimit.fromDto(v3Api.getUsage())
    }
}
