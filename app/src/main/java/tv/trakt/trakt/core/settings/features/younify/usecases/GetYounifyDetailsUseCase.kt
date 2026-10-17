package tv.trakt.trakt.core.settings.features.younify.usecases

import timber.log.Timber
import tv.trakt.trakt.common.data.remote.YounifyRemoteSource
import tv.trakt.trakt.core.settings.features.younify.model.YounifyDetails

internal class GetYounifyDetailsUseCase(
    private val remoteSource: YounifyRemoteSource,
) {
    suspend fun getYounifyDetails(generateTokens: Boolean): YounifyDetails {
        Timber.d("Getting Younify details...")

        val detailsDto = remoteSource.getYounifyDetails(
            generateTokens = generateTokens,
        )

        return YounifyDetails.fromDto(detailsDto)
            .also {
                Timber.d("Younify details: $it")
            }
    }
}
