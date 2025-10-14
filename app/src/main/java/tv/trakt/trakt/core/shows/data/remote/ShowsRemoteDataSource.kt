package tv.trakt.trakt.core.shows.data.remote

import tv.trakt.trakt.common.model.Sentiments
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.networking.CastCrewDto
import tv.trakt.trakt.common.networking.ExternalRatingsDto
import tv.trakt.trakt.common.networking.ExtraVideoDto
import tv.trakt.trakt.common.networking.RecommendedShowDto
import tv.trakt.trakt.common.networking.ShowDto
import tv.trakt.trakt.common.networking.StreamingDto
import tv.trakt.trakt.core.shows.data.remote.model.AnticipatedShowDto
import tv.trakt.trakt.core.shows.data.remote.model.TrendingShowDto
import java.time.Instant

internal interface ShowsRemoteDataSource {
    suspend fun getTrending(
        page: Int = 1,
        limit: Int,
    ): List<TrendingShowDto>

    suspend fun getHot(limit: Int): List<TrendingShowDto>

    suspend fun getPopular(
        page: Int = 1,
        limit: Int,
        years: Int,
    ): List<ShowDto>

    suspend fun getRecommended(limit: Int): List<RecommendedShowDto>

    suspend fun getAnticipated(
        page: Int = 1,
        limit: Int,
        endDate: Instant,
    ): List<AnticipatedShowDto>

    suspend fun getShowDetails(showId: TraktId): ShowDto

    suspend fun getExternalRatings(showId: TraktId): ExternalRatingsDto

    suspend fun getStudios(showId: TraktId): List<String>

    suspend fun getStreamings(
        showId: TraktId,
        countryCode: String?,
    ): Map<String, StreamingDto>

    suspend fun getCastCrew(showId: TraktId): CastCrewDto

    suspend fun getSentiments(showId: TraktId): Sentiments

    suspend fun getRelated(showId: TraktId): List<ShowDto>

    suspend fun getExtras(showId: TraktId): List<ExtraVideoDto>
}
