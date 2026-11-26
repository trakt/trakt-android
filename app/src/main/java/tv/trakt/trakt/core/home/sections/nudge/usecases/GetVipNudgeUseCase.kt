package tv.trakt.trakt.core.home.sections.nudge.usecases

import tv.trakt.trakt.common.networking.api.v3.V3Api
import tv.trakt.trakt.core.home.sections.nudge.model.VipNudge

internal class GetVipNudgeUseCase(
    private val v3Api: V3Api,
) {
    suspend fun getNudge(): VipNudge? {
        val nudge = v3Api.getNudge()
        if (nudge?.nudge == null) {
            return null
        }
        return VipNudge.fromDto(nudge, v3Api.getUsage())
    }

    suspend fun markNudgeSeen() {
        v3Api.postNudgeSeen()
    }
}
