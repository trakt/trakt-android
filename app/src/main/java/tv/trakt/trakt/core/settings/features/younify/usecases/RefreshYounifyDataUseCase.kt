package tv.trakt.trakt.core.settings.features.younify.usecases

import timber.log.Timber
import tv.trakt.trakt.common.data.remote.YounifyRemoteSource

internal class RefreshYounifyDataUseCase(
    private val remoteSource: YounifyRemoteSource,
) {
    suspend fun refresh(
        serviceId: String,
        skipSync: Boolean,
    ) {
        Timber.d("Refreshing Younify data...")
        remoteSource.postYounifyRefresh(
            serviceId = serviceId,
            skipSync = skipSync,
        )
    }
}
