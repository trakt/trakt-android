package tv.trakt.trakt.core.movies.data.remote

import kotlinx.collections.immutable.toImmutableList
import org.openapitools.client.apis.MoviesApi
import org.openapitools.client.apis.RecommendationsApi
import tv.trakt.trakt.common.model.Sentiments
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.networking.CastCrewDto
import tv.trakt.trakt.common.networking.CommentDto
import tv.trakt.trakt.common.networking.ExternalRatingsDto
import tv.trakt.trakt.common.networking.ExtraVideoDto
import tv.trakt.trakt.common.networking.MovieDto
import tv.trakt.trakt.common.networking.RecommendedMovieDto
import tv.trakt.trakt.common.networking.StreamingDto
import tv.trakt.trakt.core.movies.data.remote.model.AnticipatedMovieDto
import tv.trakt.trakt.core.movies.data.remote.model.TrendingMovieDto
import java.time.Instant

internal class MoviesApiClient(
    private val moviesApi: MoviesApi,
    private val recommendationsApi: RecommendationsApi,
) : MoviesRemoteDataSource {
    override suspend fun getTrending(
        page: Int,
        limit: Int,
    ): List<TrendingMovieDto> {
        val response = moviesApi.getMoviesTrending(
            extended = "full,streaming_ids,cloud9,colors",
            page = page,
            limit = limit,
            watchnow = null,
            genres = null,
            years = null,
            ratings = null,
            ignoreWatched = null,
            ignoreCollected = null,
            ignoreWatchlisted = null,
            startDate = null,
            endDate = null,
        )

        return response.body()
            .map {
                TrendingMovieDto(
                    watchers = it.watchers,
                    movie = it.movie,
                )
            }
    }

    override suspend fun getPopular(
        page: Int,
        limit: Int,
        years: Int,
    ): List<MovieDto> {
        val response = moviesApi.getMoviesPopular(
            extended = "full,streaming_ids,cloud9,colors",
            page = page,
            limit = limit,
            years = years.toString(),
            watchnow = null,
            genres = null,
            ratings = null,
            ignoreWatched = null,
            ignoreCollected = null,
            ignoreWatchlisted = null,
            startDate = null,
            endDate = null,
        )

        return response.body()
    }

    override suspend fun getRecommended(limit: Int): List<RecommendedMovieDto> {
        val response = recommendationsApi.getRecommendationsMoviesRecommend(
            extended = "full,streaming_ids,cloud9,colors",
            limit = limit,
            ignoreWatched = true,
            ignoreWatchlisted = true,
            ignoreCollected = true,
            watchWindow = 25,
            watchnow = null,
            genres = null,
            years = null,
            ratings = null,
            startDate = null,
            endDate = null,
        )

        return response.body()
    }

    override suspend fun getAnticipated(
        page: Int,
        limit: Int,
        endDate: Instant,
    ): List<AnticipatedMovieDto> {
        val response = moviesApi.getMoviesAnticipated(
            extended = "full,streaming_ids,cloud9,colors",
            page = page,
            limit = limit,
            watchnow = null,
            genres = null,
            years = null,
            ratings = null,
            ignoreWatched = null,
            ignoreCollected = null,
            ignoreWatchlisted = null,
            startDate = null,
            endDate = endDate.toString(),
        )

        return response.body()
            .map {
                AnticipatedMovieDto(
                    listCount = it.listCount,
                    movie = it.movie,
                )
            }
    }

    override suspend fun getDetails(movieId: TraktId): MovieDto {
        val response = moviesApi.getMoviesSummary(
            id = movieId.value.toString(),
            extended = "full,streaming_ids,cloud9,colors",
        )

        return response.body()
    }

    override suspend fun getExternalRatings(movieId: TraktId): ExternalRatingsDto {
        val response = moviesApi.getMoviesRatings(
            id = movieId.value.toString(),
            extended = "all",
        )

        return response.body()
    }

    override suspend fun getStudios(movieId: TraktId): List<String> {
        val response = moviesApi.getMoviesStudios(
            id = movieId.value.toString(),
        )

        return response.body().map { it.name }
    }

    override suspend fun getStreamings(
        movieId: TraktId,
        countryCode: String?,
    ): Map<String, StreamingDto> {
        val response = moviesApi.getMoviesWatchnow(
            country = countryCode ?: "",
            id = movieId.value.toString(),
            links = "direct",
        )

        return response.body()
    }

    override suspend fun getExtras(movieId: TraktId): List<ExtraVideoDto> {
        val response = moviesApi.getMoviesVideos(
            id = movieId.value.toString(),
        )
        return response.body()
    }

    override suspend fun getCastCrew(movieId: TraktId): CastCrewDto {
        val response = moviesApi.getMoviesPeople(
            id = movieId.value.toString(),
            extended = "cloud9",
        )
        return response.body()
    }

    override suspend fun getRelated(movieId: TraktId): List<MovieDto> {
        val response = moviesApi.getMoviesRelated(
            id = movieId.value.toString(),
            extended = "full,streaming_ids,cloud9,colors",
            limit = 30,
            page = null,
        )
        return response.body()
    }

    override suspend fun getSentiments(movieId: TraktId): Sentiments {
        val response = moviesApi.getMoviesSentiments(
            id = movieId.value.toString(),
        ).body()

        return Sentiments(
            good = response.good
                .map { Sentiments.Sentiment(it.sentiment) }
                .toImmutableList(),
            bad = response.bad
                .map { Sentiments.Sentiment(it.sentiment) }
                .toImmutableList(),
        )
    }

    override suspend fun getComments(
        movieId: TraktId,
        limit: Int,
    ): List<CommentDto> {
        val response = moviesApi.getMoviesComments(
            id = movieId.value.toString(),
            sort = "likes",
            extended = "cloud9",
            page = null,
            limit = limit.toString(),
        )

        return response.body()
    }
}
