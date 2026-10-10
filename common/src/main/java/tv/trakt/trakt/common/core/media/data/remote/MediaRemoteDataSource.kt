package tv.trakt.trakt.common.core.media.data.remote

import tv.trakt.trakt.common.model.globalfilter.GlobalFilter
import tv.trakt.trakt.common.networking.AnticipatedMediaDto
import tv.trakt.trakt.common.networking.PopularMediaDto
import tv.trakt.trakt.common.networking.TrendingMediaDto
import tv.trakt.trakt.common.networking.api.v3.model.V3MediaRecommendationResponse

interface MediaRemoteDataSource {
    suspend fun getTrending(
        page: Int = 1,
        limit: Int,
        filters: GlobalFilter,
    ): List<TrendingMediaDto>

    suspend fun getPopular(
        page: Int = 1,
        limit: Int,
        filters: GlobalFilter,
    ): List<PopularMediaDto>

    suspend fun getAnticipated(
        page: Int = 1,
        limit: Int,
        filters: GlobalFilter,
    ): List<AnticipatedMediaDto>

    suspend fun getRecommended(
        limit: Int,
        filters: GlobalFilter,
    ): List<V3MediaRecommendationResponse>
}
