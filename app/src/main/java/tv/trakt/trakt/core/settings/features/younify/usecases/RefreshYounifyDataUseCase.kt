package tv.trakt.trakt.core.settings.features.younify.usecases

import timber.log.Timber
import tv.trakt.trakt.core.settings.features.younify.data.remote.YounifyRemoteDataSource

internal class RefreshYounifyDataUseCase(
    private val remoteSource: YounifyRemoteDataSource,
) {
    suspend fun refresh(
        serviceId: String,
        skipSync: Boolean,
    ) {
        Timber.d("Refreshing Younify data...")
        remoteSource.postYounifyRefresh(
            serviceID = serviceId,
            skipSync = skipSync,
        )
    }
}
