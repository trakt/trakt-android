package tv.trakt.trakt.app.core.shows.data.remote

import tv.trakt.trakt.app.core.shows.data.remote.model.response.AnticipatedShowDto
import tv.trakt.trakt.app.core.shows.data.remote.model.response.TrendingShowDto
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.networking.CalendarMediaDto
import tv.trakt.trakt.common.networking.CastCrewDto
import tv.trakt.trakt.common.networking.CommentDto
import tv.trakt.trakt.common.networking.ExternalShowRatingsDto
import tv.trakt.trakt.common.networking.ExtraVideoDto
import tv.trakt.trakt.common.networking.ListDto
import tv.trakt.trakt.common.networking.RecommendedShowDto
import tv.trakt.trakt.common.networking.SeasonDto
import tv.trakt.trakt.common.networking.ShowCalendarsDto
import tv.trakt.trakt.common.networking.StreamingDto
import java.time.Instant

internal interface ShowsRemoteDataSource {
    suspend fun getTrendingShows(
        limit: Int,
        page: Int,
    ): List<TrendingShowDto>

    suspend fun getPopularShows(
        limit: Int,
        page: Int,
        years: Int? = null,
    ): List<ShowCalendarsDto>

    suspend fun getAnticipatedShows(
        limit: Int,
        page: Int,
        endDate: Instant,
    ): List<AnticipatedShowDto>

    suspend fun getRecommendedShows(
        limit: Int,
        page: Int,
    ): List<RecommendedShowDto>

    suspend fun getReleases(
        startDate: Instant,
        days: Int,
    ): List<CalendarMediaDto>

    suspend fun getRelatedShows(showId: TraktId): List<ShowCalendarsDto>

    suspend fun getShowDetails(showId: TraktId): ShowCalendarsDto

    suspend fun getShowExternalRatings(showId: TraktId): ExternalShowRatingsDto

    suspend fun getShowExtras(showId: TraktId): List<ExtraVideoDto>

    suspend fun getShowCastCrew(showId: TraktId): CastCrewDto

    suspend fun getShowComments(showId: TraktId): List<CommentDto>

    suspend fun getShowLists(
        showId: TraktId,
        type: String,
        limit: Int,
    ): List<ListDto>

    suspend fun getShowStreamings(
        showId: TraktId,
        countryCode: String?,
    ): Map<String, StreamingDto>

    suspend fun getShowSeasons(showId: TraktId): List<SeasonDto>
}
