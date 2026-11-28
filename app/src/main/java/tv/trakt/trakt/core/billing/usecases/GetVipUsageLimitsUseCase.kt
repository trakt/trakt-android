package tv.trakt.trakt.core.billing.usecases

import kotlinx.collections.immutable.ImmutableList
import tv.trakt.trakt.common.auth.session.SessionManager
import tv.trakt.trakt.common.networking.api.v3.V3UserApi
import tv.trakt.trakt.core.billing.model.VipUsageGroup
import tv.trakt.trakt.core.billing.model.VipUsageLimit
import tv.trakt.trakt.resources.R

internal class GetVipUsageLimitsUseCase(
    private val v3UserApi: V3UserApi,
    private val sessionManager: SessionManager,
) {
    suspend fun getLimits(): ImmutableList<VipUsageLimit>? {
        if (!sessionManager.isAuthenticated()) {
            return null
        }
        return VipUsageLimit.fromDto(v3UserApi.getUsage())
    }

    suspend fun getGroupedLimits(): ImmutableList<VipUsageGroup>? {
        if (!sessionManager.isAuthenticated()) {
            return null
        }
        return VipUsageGroup.fromDto(v3UserApi.getUsage())
    }

    suspend fun getHistoryLimit(): VipUsageLimit? {
        if (!sessionManager.isAuthenticated()) {
            return null
        }
        return VipUsageLimit.fromItem(R.string.limit_title_watch_history, v3UserApi.getUsage().history)
    }
}
