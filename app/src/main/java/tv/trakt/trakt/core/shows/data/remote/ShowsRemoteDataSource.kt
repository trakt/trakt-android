package tv.trakt.trakt.core.shows.data.remote

import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.model.globalfilter.GlobalFilter
import tv.trakt.trakt.common.networking.CalendarMediaDto
import tv.trakt.trakt.common.networking.CastCrewDto
import tv.trakt.trakt.common.networking.CommentDto
import tv.trakt.trakt.common.networking.ExternalRatingsDto
import tv.trakt.trakt.common.networking.ExtraVideoDto
import tv.trakt.trakt.common.networking.ListDto
import tv.trakt.trakt.common.networking.RecommendedShowDto
import tv.trakt.trakt.common.networking.SeasonDto
import tv.trakt.trakt.common.networking.ShowCalendarsDto
import tv.trakt.trakt.common.networking.ShowStatsDto
import tv.trakt.trakt.common.networking.StreamingDto
import tv.trakt.trakt.common.networking.api.v3.model.V3SentimentResponse
import tv.trakt.trakt.core.shows.data.remote.model.AnticipatedShowDto
import tv.trakt.trakt.core.shows.data.remote.model.TrendingShowDto
import java.time.Instant

internal interface ShowsRemoteDataSource {
    suspend fun getTrending(
        page: Int = 1,
        limit: Int,
        filters: GlobalFilter,
    ): List<TrendingShowDto>

    suspend fun getPopular(
        page: Int = 1,
        limit: Int,
        filters: GlobalFilter,
    ): List<ShowCalendarsDto>

    suspend fun getRecommended(
        limit: Int,
        filters: GlobalFilter,
    ): List<RecommendedShowDto>

    suspend fun getAnticipated(
        page: Int = 1,
        limit: Int,
        endDate: Instant? = null,
        filters: GlobalFilter,
    ): List<AnticipatedShowDto>

    suspend fun getReleases(
        startDate: Instant,
        days: Int,
        filters: GlobalFilter,
    ): List<CalendarMediaDto>

    suspend fun getShowDetails(showId: TraktId): ShowCalendarsDto

    suspend fun getExternalRatings(showId: TraktId): ExternalRatingsDto

    suspend fun getStudios(showId: TraktId): List<String>

    suspend fun getStats(showId: TraktId): ShowStatsDto

    suspend fun getStreamings(
        showId: TraktId,
        countryCode: String?,
    ): Map<String, StreamingDto>

    suspend fun getJustWatchLink(
        showId: TraktId,
        countryCode: String?,
    ): String?

    suspend fun getCastCrew(showId: TraktId): CastCrewDto

    suspend fun getSentiments(showId: TraktId): V3SentimentResponse?

    suspend fun getRelated(showId: TraktId): List<ShowCalendarsDto>

    suspend fun getLists(
        showId: TraktId,
        type: String,
        limit: Int,
    ): List<ListDto>

    suspend fun getComments(
        showId: TraktId,
        limit: Int,
        sort: String,
    ): List<CommentDto>

    suspend fun getExtras(showId: TraktId): List<ExtraVideoDto>

    suspend fun getSeasons(showId: TraktId): List<SeasonDto>
}
