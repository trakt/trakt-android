package tv.trakt.trakt.common.data.remote

import tv.trakt.trakt.common.data.dto.PortraitTokenDto
import tv.trakt.trakt.common.networking.api.portraits.PortraitsApi

class PortraitsRemoteSource(
    private val portraitsApi: PortraitsApi,
) {
    suspend fun getToken(): PortraitTokenDto {
        return portraitsApi.getToken()
    }
}
