package tv.trakt.trakt.core.settings.features.younify.data.remote

import tv.trakt.trakt.core.settings.features.younify.data.remote.model.dto.YounifyDetailsDto

internal interface YounifyRemoteDataSource {
    suspend fun getYounifyDetails(generateTokens: Boolean): YounifyDetailsDto

    suspend fun postYounifyRefresh(
        serviceId: String,
        skipSync: Boolean,
    )

    suspend fun postYounifyUnlink(serviceId: String)
}
