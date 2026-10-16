package tv.trakt.trakt.common.core.movies.data.remote

import tv.trakt.trakt.common.core.movies.data.remote.model.AnticipatedMovieDto
import tv.trakt.trakt.common.core.movies.data.remote.model.TrendingMovieDto
import tv.trakt.trakt.common.model.SlugId
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.model.globalfilter.GlobalFilter
import tv.trakt.trakt.common.networking.CalendarMediaDto
import tv.trakt.trakt.common.networking.CastCrewDto
import tv.trakt.trakt.common.networking.CommentDto
import tv.trakt.trakt.common.networking.ExternalMovieRatingsDto
import tv.trakt.trakt.common.networking.ExtraVideoDto
import tv.trakt.trakt.common.networking.ListDto
import tv.trakt.trakt.common.networking.MovieCalendarDto
import tv.trakt.trakt.common.networking.MovieStatsDto
import tv.trakt.trakt.common.networking.RecommendedMovieDto
import tv.trakt.trakt.common.networking.api.v3.model.V3MovieRecommendationResponse
import tv.trakt.trakt.common.networking.api.v3.model.V3SentimentResponse
import java.time.Instant

interface MoviesRemoteDataSource {
    suspend fun getTrending(
        page: Int = 1,
        limit: Int,
        filters: GlobalFilter = GlobalFilter.Default,
        subgenres: List<String>? = null,
        theme: String? = null,
    ): List<TrendingMovieDto>

    suspend fun getPopular(
        page: Int = 1,
        limit: Int,
        filters: GlobalFilter = GlobalFilter.Default,
        theme: String? = null,
    ): List<MovieCalendarDto>

    suspend fun getRecommended(
        limit: Int,
        filters: GlobalFilter = GlobalFilter.Default,
    ): List<V3MovieRecommendationResponse>

    suspend fun getRecommendedV2(limit: Int): List<RecommendedMovieDto>

    suspend fun getAnticipated(
        page: Int = 1,
        limit: Int,
        endDate: Instant? = null,
        filters: GlobalFilter = GlobalFilter.Default,
        theme: String? = null,
    ): List<AnticipatedMovieDto>

    suspend fun getReleases(
        startDate: Instant,
        days: Int,
        filters: GlobalFilter = GlobalFilter.Default,
    ): List<CalendarMediaDto>

    suspend fun getDetails(movieId: TraktId): MovieCalendarDto

    suspend fun getDetails(movieSlug: SlugId): MovieCalendarDto

    suspend fun getExternalRatings(movieId: TraktId): ExternalMovieRatingsDto

    suspend fun getStudios(movieId: TraktId): List<String>

    suspend fun getStats(movieId: TraktId): MovieStatsDto

    suspend fun getExtras(movieId: TraktId): List<ExtraVideoDto>

    suspend fun getCastCrew(
        movieId: TraktId,
        extended: String = "cloud9,full,markdown",
    ): CastCrewDto

    suspend fun getRelated(
        movieId: TraktId,
        limit: Int = 30,
    ): List<MovieCalendarDto>

    suspend fun getSentiments(movieId: TraktId): V3SentimentResponse?

    suspend fun getComments(
        movieId: TraktId,
        limit: Int,
        sort: String,
        language: String? = null,
        extended: String = "full,images,vip",
    ): List<CommentDto>

    suspend fun getLists(
        movieId: TraktId,
        type: String,
        limit: Int,
    ): List<ListDto>
}
