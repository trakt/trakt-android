package tv.trakt.trakt.core.movies.data.remote

import tv.trakt.trakt.common.networking.MovieDto
import tv.trakt.trakt.common.networking.RecommendedMovieDto
import tv.trakt.trakt.core.movies.data.remote.model.AnticipatedMovieDto
import tv.trakt.trakt.core.movies.data.remote.model.TrendingMovieDto

internal interface MoviesRemoteDataSource {
    suspend fun getTrending(limit: Int): List<TrendingMovieDto>

    suspend fun getHot(limit: Int): List<TrendingMovieDto>

    suspend fun getPopular(
        limit: Int,
        years: Int,
    ): List<MovieDto>

    suspend fun getRecommended(limit: Int): List<RecommendedMovieDto>

    suspend fun getAnticipated(limit: Int): List<AnticipatedMovieDto>
}
