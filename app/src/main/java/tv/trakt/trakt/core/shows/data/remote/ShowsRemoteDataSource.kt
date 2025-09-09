package tv.trakt.trakt.core.shows.data.remote

import tv.trakt.trakt.common.networking.RecommendedShowDto
import tv.trakt.trakt.common.networking.ShowDto
import tv.trakt.trakt.core.shows.data.remote.model.AnticipatedShowDto
import tv.trakt.trakt.core.shows.data.remote.model.TrendingShowDto
import java.time.Instant

internal interface ShowsRemoteDataSource {
    suspend fun getTrending(limit: Int): List<TrendingShowDto>

    suspend fun getHot(limit: Int): List<TrendingShowDto>

    suspend fun getPopular(
        limit: Int,
        years: Int,
    ): List<ShowDto>

    suspend fun getRecommended(limit: Int): List<RecommendedShowDto>

    suspend fun getAnticipated(
        limit: Int,
        endDate: Instant,
    ): List<AnticipatedShowDto>
}
