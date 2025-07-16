package tv.trakt.app.tv.core.movies.data.remote

import tv.trakt.app.tv.common.model.TraktId
import tv.trakt.app.tv.core.movies.data.remote.model.response.AnticipatedMovieDto
import tv.trakt.app.tv.core.movies.data.remote.model.response.TrendingMovieDto
import tv.trakt.app.tv.networking.openapi.CastCrewDto
import tv.trakt.app.tv.networking.openapi.CommentDto
import tv.trakt.app.tv.networking.openapi.ExternalRatingsDto
import tv.trakt.app.tv.networking.openapi.ExtraVideoDto
import tv.trakt.app.tv.networking.openapi.ListDto
import tv.trakt.app.tv.networking.openapi.MovieDto
import tv.trakt.app.tv.networking.openapi.RecommendedMovieDto
import tv.trakt.app.tv.networking.openapi.StreamingDto

internal interface MoviesRemoteDataSource {
    suspend fun getTrendingMovies(): List<TrendingMovieDto>

    suspend fun getMonthlyHotMovies(): List<TrendingMovieDto>

    suspend fun getPopularMovies(): List<MovieDto>

    suspend fun getAnticipatedMovies(): List<AnticipatedMovieDto>

    suspend fun getRecommendedMovies(): List<RecommendedMovieDto>

    suspend fun getMovieDetails(movieId: TraktId): MovieDto

    suspend fun getRelatedMovies(movieId: TraktId): List<MovieDto>

    suspend fun getMovieExternalRatings(movieId: TraktId): ExternalRatingsDto

    suspend fun getMovieExtras(movieId: TraktId): List<ExtraVideoDto>

    suspend fun getMovieCastCrew(movieId: TraktId): CastCrewDto

    suspend fun getMovieComments(movieId: TraktId): List<CommentDto>

    suspend fun getMovieLists(
        movieId: TraktId,
        type: String,
        limit: Int,
    ): List<ListDto>

    suspend fun getMovieStreamings(
        movieId: TraktId,
        countryCode: String,
    ): Map<String, StreamingDto>
}
