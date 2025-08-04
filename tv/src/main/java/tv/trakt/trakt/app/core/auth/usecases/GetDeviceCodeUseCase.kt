package tv.trakt.trakt.app.core.auth.usecases

import tv.trakt.trakt.app.core.auth.data.remote.AuthRemoteDataSource
import tv.trakt.trakt.app.core.auth.model.AuthDeviceCode

internal class GetDeviceCodeUseCase(
    private val remoteSource: AuthRemoteDataSource,
) {
    suspend fun getDeviceCode(): AuthDeviceCode {
        return remoteSource.getDeviceCode()
    }
}
