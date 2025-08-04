package tv.trakt.trakt.app.core.auth.usecases

import tv.trakt.trakt.app.core.auth.data.remote.AuthRemoteDataSource
import tv.trakt.trakt.app.core.auth.model.AuthDeviceTokenState
import tv.trakt.trakt.app.core.auth.model.AuthDeviceTokenState.Success
import tv.trakt.trakt.common.auth.TokenProvider

internal class GetDeviceTokenUseCase(
    private val remoteSource: AuthRemoteDataSource,
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
