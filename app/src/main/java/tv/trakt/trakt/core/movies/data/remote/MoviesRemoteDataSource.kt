package tv.trakt.trakt.core.movies.data.remote

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
import tv.trakt.trakt.common.networking.api.v3.model.V3SentimentResponse
import tv.trakt.trakt.core.movies.data.remote.model.AnticipatedMovieDto
import tv.trakt.trakt.core.movies.data.remote.model.TrendingMovieDto
import java.time.Instant

internal interface MoviesRemoteDataSource {
    suspend fun getTrending(
        page: Int = 1,
        limit: Int,
        filters: GlobalFilter,
        subgenres: List<String>? = null,
    ): List<TrendingMovieDto>

    suspend fun getPopular(
        page: Int = 1,
        limit: Int,
        filters: GlobalFilter,
    ): List<MovieCalendarDto>

    suspend fun getRecommended(
        limit: Int,
        filters: GlobalFilter,
    ): List<RecommendedMovieDto>

    suspend fun getAnticipated(
        page: Int = 1,
        limit: Int,
        endDate: Instant? = null,
        filters: GlobalFilter,
    ): List<AnticipatedMovieDto>

    suspend fun getReleases(
        startDate: Instant,
        days: Int,
        filters: GlobalFilter,
    ): List<CalendarMediaDto>

    suspend fun getDetails(movieId: TraktId): MovieCalendarDto

    suspend fun getExternalRatings(movieId: TraktId): ExternalMovieRatingsDto

    suspend fun getStudios(movieId: TraktId): List<String>

    suspend fun getStats(movieId: TraktId): MovieStatsDto

    suspend fun getExtras(movieId: TraktId): List<ExtraVideoDto>

    suspend fun getCastCrew(movieId: TraktId): CastCrewDto

    suspend fun getRelated(movieId: TraktId): List<MovieCalendarDto>

    suspend fun getSentiments(movieId: TraktId): V3SentimentResponse?

    suspend fun getComments(
        movieId: TraktId,
        limit: Int,
        sort: String,
        language: String? = null,
    ): List<CommentDto>

    suspend fun getLists(
        movieId: TraktId,
        type: String,
        limit: Int,
    ): List<ListDto>
}
