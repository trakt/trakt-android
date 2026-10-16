package tv.trakt.trakt.app.core.auth.usecases

import tv.trakt.trakt.common.auth.TokenProvider
import tv.trakt.trakt.common.auth.data.remote.AuthRemoteSource
import tv.trakt.trakt.common.auth.model.AuthDeviceTokenState
import tv.trakt.trakt.common.auth.model.AuthDeviceTokenState.Success

internal class GetDeviceTokenUseCase(
    private val remoteSource: AuthRemoteSource,
    private val tokenProvider: TokenProvider,
) {
    suspend fun getDeviceToken(deviceCode: String): AuthDeviceTokenState {
        val tokenState = remoteSource.getDeviceToken(deviceCode)

        if (tokenState is Success) {
            tokenProvider.saveToken(tokenState.token)
        }

        return tokenState
    }
}
