package tv.trakt.trakt.common.core.younify.data.remote

import tv.trakt.trakt.common.core.younify.data.remote.model.dto.YounifyDetailsDto

interface YounifyRemoteDataSource {
    suspend fun getYounifyDetails(generateTokens: Boolean): YounifyDetailsDto

    suspend fun postYounifyRefresh(
        serviceId: String,
        skipSync: Boolean,
    )

    suspend fun postYounifyUnlink(serviceId: String)

    suspend fun clear()
}
