package tv.trakt.trakt.core.movies.data.remote

import tv.trakt.trakt.common.model.Sentiments
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.networking.CastCrewDto
import tv.trakt.trakt.common.networking.CommentDto
import tv.trakt.trakt.common.networking.ExternalRatingsDto
import tv.trakt.trakt.common.networking.ExtraVideoDto
import tv.trakt.trakt.common.networking.ListDto
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
        genres: List<String>? = null,
        subgenres: List<String>? = null,
        years: String? = null,
    ): List<TrendingMovieDto>

    suspend fun getPopular(
        page: Int = 1,
        limit: Int,
        years: String? = null,
        genres: List<String>? = null,
        subgenres: List<String>? = null,
    ): List<MovieDto>

    suspend fun getRecommended(
        limit: Int,
        years: String? = null,
        genres: List<String>? = null,
        subgenres: List<String>? = null,
    ): List<RecommendedMovieDto>

    suspend fun getAnticipated(
        page: Int = 1,
        limit: Int,
        endDate: Instant? = null,
        genres: List<String>? = null,
        subgenres: List<String>? = null,
        years: String? = null,
    ): List<AnticipatedMovieDto>

    suspend fun getDetails(movieId: TraktId): MovieDto

    suspend fun getExternalRatings(movieId: TraktId): ExternalRatingsDto

    suspend fun getStudios(movieId: TraktId): List<String>

    suspend fun getStreamings(
        movieId: TraktId,
        countryCode: String?,
    ): Map<String, StreamingDto>

    suspend fun getJustWatchLink(
        movieId: TraktId,
        countryCode: String?,
    ): String?

    suspend fun getExtras(movieId: TraktId): List<ExtraVideoDto>

    suspend fun getCastCrew(movieId: TraktId): CastCrewDto

    suspend fun getRelated(movieId: TraktId): List<MovieDto>

    suspend fun getSentiments(movieId: TraktId): Sentiments

    suspend fun getComments(
        movieId: TraktId,
        limit: Int,
        sort: String,
    ): List<CommentDto>

    suspend fun getLists(
        movieId: TraktId,
        type: String,
        limit: Int,
    ): List<ListDto>
}
