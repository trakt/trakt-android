package tv.trakt.trakt.core.settings.features.younify.usecases

import tv.trakt.trakt.core.settings.features.younify.data.remote.YounifyRemoteDataSource
import tv.trakt.trakt.core.settings.features.younify.data.remote.model.YounifyDetails

internal class GetYounifyDetailsUseCase(
    private val remoteSource: YounifyRemoteDataSource,
) {
    suspend fun getYounifyDetails(generateTokens: Boolean): YounifyDetails {
        val detailsDto = remoteSource.getYounifyDetails(
            generateTokens = generateTokens,
        )
        return YounifyDetails.fromDto(detailsDto)
    }
}
