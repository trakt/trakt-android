package tv.trakt.trakt.core.settings.features.younify.usecases

import timber.log.Timber
import tv.trakt.trakt.core.settings.features.younify.data.remote.YounifyRemoteDataSource

internal class UnlinkYounifyServiceUseCase(
    private val remoteSource: YounifyRemoteDataSource,
) {
    suspend fun unlinkService(serviceId: String) {
        Timber.d("Unlinking Younify service: $serviceId")
        remoteSource.postYounifyUnlink(
            serviceId = serviceId,
        )
    }
}
