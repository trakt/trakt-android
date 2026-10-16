package tv.trakt.trakt.common.core.shows.data.remote

import tv.trakt.trakt.common.core.shows.data.remote.model.AnticipatedShowDto
import tv.trakt.trakt.common.core.shows.data.remote.model.TrendingShowDto
import tv.trakt.trakt.common.model.SlugId
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.model.globalfilter.GlobalFilter
import tv.trakt.trakt.common.networking.CalendarMediaDto
import tv.trakt.trakt.common.networking.CalendarShowDto
import tv.trakt.trakt.common.networking.CastCrewDto
import tv.trakt.trakt.common.networking.CommentDto
import tv.trakt.trakt.common.networking.ExternalShowRatingsDto
import tv.trakt.trakt.common.networking.ExtraVideoDto
import tv.trakt.trakt.common.networking.ListDto
import tv.trakt.trakt.common.networking.RecommendedShowDto
import tv.trakt.trakt.common.networking.SeasonDto
import tv.trakt.trakt.common.networking.ShowCalendarsDto
import tv.trakt.trakt.common.networking.ShowStatsDto
import tv.trakt.trakt.common.networking.api.v3.model.V3SentimentResponse
import tv.trakt.trakt.common.networking.api.v3.model.V3ShowRecommendationResponse
import java.time.Instant

interface ShowsRemoteDataSource {
    suspend fun getTrending(
        page: Int = 1,
        limit: Int,
        filters: GlobalFilter = GlobalFilter.Default,
        theme: String? = null,
    ): List<TrendingShowDto>

    suspend fun getPopular(
        page: Int = 1,
        limit: Int,
        filters: GlobalFilter = GlobalFilter.Default,
        theme: String? = null,
    ): List<ShowCalendarsDto>

    suspend fun getRecommended(
        limit: Int,
        filters: GlobalFilter = GlobalFilter.Default,
    ): List<V3ShowRecommendationResponse>

    suspend fun getRecommendedV2(limit: Int): List<RecommendedShowDto>

    suspend fun getAnticipated(
        page: Int = 1,
        limit: Int,
        endDate: Instant? = null,
        filters: GlobalFilter = GlobalFilter.Default,
        theme: String? = null,
    ): List<AnticipatedShowDto>

    suspend fun getReleases(
        startDate: Instant,
        days: Int,
        filters: GlobalFilter = GlobalFilter.Default,
    ): List<CalendarMediaDto>

    suspend fun getReleasesPremieres(
        startDate: Instant,
        days: Int,
        filters: GlobalFilter = GlobalFilter.Default,
    ): List<CalendarShowDto>

    suspend fun getReleasesFinales(
        startDate: Instant,
        days: Int,
        filters: GlobalFilter = GlobalFilter.Default,
    ): List<CalendarShowDto>

    suspend fun getShowDetails(showId: TraktId): ShowCalendarsDto

    suspend fun getShowDetails(showSlug: SlugId): ShowCalendarsDto

    suspend fun getExternalRatings(showId: TraktId): ExternalShowRatingsDto

    suspend fun getStudios(showId: TraktId): List<String>

    suspend fun getStats(showId: TraktId): ShowStatsDto

    suspend fun getCastCrew(
        showId: TraktId,
        guestStars: Boolean = false,
        extended: String? = null,
    ): CastCrewDto

    suspend fun getSentiments(showId: TraktId): V3SentimentResponse?

    suspend fun getRelated(
        showId: TraktId,
        limit: Int = 30,
    ): List<ShowCalendarsDto>

    suspend fun getLists(
        showId: TraktId,
        type: String,
        limit: Int,
    ): List<ListDto>

    suspend fun getComments(
        showId: TraktId,
        limit: Int,
        sort: String,
        language: String? = null,
        extended: String = "full,images,vip",
    ): List<CommentDto>

    suspend fun getExtras(showId: TraktId): List<ExtraVideoDto>

    suspend fun getSeasons(showId: TraktId): List<SeasonDto>
}
