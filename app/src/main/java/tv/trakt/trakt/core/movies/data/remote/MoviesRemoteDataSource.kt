package tv.trakt.trakt.core.movies.data.remote

import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.networking.ExternalRatingsDto
import tv.trakt.trakt.common.networking.MovieDto
import tv.trakt.trakt.common.networking.RecommendedMovieDto
import tv.trakt.trakt.common.networking.StreamingDto
import tv.trakt.trakt.core.movies.data.remote.model.AnticipatedMovieDto
import tv.trakt.trakt.core.movies.data.remote.model.TrendingMovieDto
import java.time.Instant

internal interface MoviesRemoteDataSource {
    suspend fun getTrending(
        page: Int = 1,
        limit: Int,
    ): List<TrendingMovieDto>

    suspend fun getPopular(
        page: Int = 1,
        limit: Int,
        years: Int,
    ): List<MovieDto>

    suspend fun getRecommended(limit: Int): List<RecommendedMovieDto>

    suspend fun getAnticipated(
        page: Int = 1,
        limit: Int,
        endDate: Instant,
    ): List<AnticipatedMovieDto>

    suspend fun getDetails(movieId: TraktId): MovieDto

    suspend fun getExternalRatings(movieId: TraktId): ExternalRatingsDto

    suspend fun getStudios(movieId: TraktId): List<String>

    suspend fun getStreamings(
        movieId: TraktId,
        countryCode: String?,
    ): Map<String, StreamingDto>
}
