package tv.trakt.trakt.core.settings.features.younify.usecases

import timber.log.Timber
import tv.trakt.trakt.core.settings.features.younify.data.remote.YounifyRemoteDataSource
import tv.trakt.trakt.core.settings.features.younify.model.YounifyDetails

internal class RefreshYounifyTokensUseCase(
    private val remoteSource: YounifyRemoteDataSource,
) {
    suspend fun refreshTokens(): YounifyDetails {
        Timber.d("Refreshing Younify tokens...")

        val detailsDto = remoteSource.getYounifyDetails(
            generateTokens = true,
        )

        return YounifyDetails.fromDto(detailsDto)
    }
}
