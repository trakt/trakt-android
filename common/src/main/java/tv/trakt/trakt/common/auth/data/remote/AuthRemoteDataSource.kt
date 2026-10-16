package tv.trakt.trakt.common.auth.data.remote

import tv.trakt.trakt.common.auth.model.AuthDeviceCode
import tv.trakt.trakt.common.auth.model.AuthDeviceTokenState
import tv.trakt.trakt.common.auth.model.TraktAccessToken

interface AuthRemoteDataSource {
    suspend fun getAccessToken(
        code: String,
        codeVerifier: String,
        redirectUri: String,
    ): TraktAccessToken

    suspend fun getDeviceCode(): AuthDeviceCode

    suspend fun getDeviceToken(deviceCode: String): AuthDeviceTokenState
}
