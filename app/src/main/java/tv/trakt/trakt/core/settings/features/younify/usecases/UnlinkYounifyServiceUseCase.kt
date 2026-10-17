package tv.trakt.trakt.core.settings.features.younify.usecases

import timber.log.Timber
import tv.trakt.trakt.common.data.remote.YounifyRemoteSource

internal class UnlinkYounifyServiceUseCase(
    private val remoteSource: YounifyRemoteSource,
) {
    suspend fun unlinkService(serviceId: String) {
        Timber.d("Unlinking Younify service: $serviceId")
        remoteSource.postYounifyUnlink(
            serviceId = serviceId,
        )
    }
}
