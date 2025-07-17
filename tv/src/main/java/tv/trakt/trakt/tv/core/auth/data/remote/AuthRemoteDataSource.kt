package tv.trakt.trakt.tv.core.auth.data.remote

import tv.trakt.trakt.tv.core.auth.model.AuthDeviceCode
import tv.trakt.trakt.tv.core.auth.model.AuthDeviceTokenState

internal interface AuthRemoteDataSource {
    suspend fun getDeviceCode(): AuthDeviceCode

    suspend fun getDeviceToken(deviceCode: String): AuthDeviceTokenState
}
