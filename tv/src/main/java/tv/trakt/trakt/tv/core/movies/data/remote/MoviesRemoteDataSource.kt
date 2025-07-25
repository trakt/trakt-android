package tv.trakt.trakt.tv.core.movies.data.remote

import tv.trakt.trakt.common.networking.CastCrewDto
import tv.trakt.trakt.common.networking.CommentDto
import tv.trakt.trakt.common.networking.ExternalRatingsDto
import tv.trakt.trakt.common.networking.ExtraVideoDto
import tv.trakt.trakt.common.networking.ListDto
import tv.trakt.trakt.common.networking.MovieDto
import tv.trakt.trakt.common.networking.RecommendedMovieDto
import tv.trakt.trakt.common.networking.StreamingDto
import tv.trakt.trakt.tv.common.model.TraktId
import tv.trakt.trakt.tv.core.movies.data.remote.model.response.AnticipatedMovieDto
import tv.trakt.trakt.tv.core.movies.data.remote.model.response.TrendingMovieDto

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
