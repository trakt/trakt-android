package tv.trakt.trakt.core.settings.features.younify.usecases

import timber.log.Timber
import tv.trakt.trakt.common.data.remote.YounifyRemoteSource
import tv.trakt.trakt.core.settings.features.younify.model.YounifyDetails

internal class RefreshYounifyTokensUseCase(
    private val remoteSource: YounifyRemoteSource,
) {
    suspend fun refreshTokens(): YounifyDetails {
        Timber.d("Refreshing Younify tokens...")

        val detailsDto = remoteSource.getYounifyDetails(
            generateTokens = true,
        )

        return YounifyDetails.fromDto(detailsDto)
    }
}
