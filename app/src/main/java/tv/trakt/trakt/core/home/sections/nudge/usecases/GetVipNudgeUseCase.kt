package tv.trakt.trakt.core.home.sections.nudge.usecases

import tv.trakt.trakt.common.networking.api.v3.V3UserApi
import tv.trakt.trakt.core.home.sections.nudge.model.VipNudge

internal class GetVipNudgeUseCase(
    private val v3UserApi: V3UserApi,
) {
    suspend fun getNudge(): VipNudge? {
        val nudge = v3UserApi.getNudge()
        if (nudge?.nudge == null) {
            return null
        }
        return VipNudge.fromDto(nudge, v3UserApi.getUsage())
    }

    suspend fun markNudgeSeen() {
        v3UserApi.postNudgeSeen()
    }
}
