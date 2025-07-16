package tv.trakt.app.tv.core.auth.usecases

import tv.trakt.app.tv.core.auth.data.remote.AuthRemoteDataSource
import tv.trakt.app.tv.core.auth.model.AuthDeviceCode

internal class GetDeviceCodeUseCase(
    private val remoteSource: AuthRemoteDataSource,
) {
    suspend fun getDeviceCode(): AuthDeviceCode {
        return remoteSource.getDeviceCode()
    }
}
