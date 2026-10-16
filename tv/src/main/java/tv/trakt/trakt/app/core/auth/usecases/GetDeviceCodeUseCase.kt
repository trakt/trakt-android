package tv.trakt.trakt.app.core.auth.usecases

import tv.trakt.trakt.common.auth.data.remote.AuthRemoteSource
import tv.trakt.trakt.common.auth.model.AuthDeviceCode

internal class GetDeviceCodeUseCase(
    private val remoteSource: AuthRemoteSource,
) {
    suspend fun getDeviceCode(): AuthDeviceCode {
        return remoteSource.getDeviceCode()
    }
}
