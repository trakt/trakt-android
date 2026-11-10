package tv.trakt.trakt.app.core.auth.usecases

import tv.trakt.trakt.common.auth.model.AuthDeviceCode
import tv.trakt.trakt.common.data.remote.AuthRemoteSource

internal class GetDeviceCodeUseCase(
    private val remoteSource: AuthRemoteSource,
) {
    suspend fun getDeviceCode(): AuthDeviceCode {
        return remoteSource.getDeviceCode()
    }
}
