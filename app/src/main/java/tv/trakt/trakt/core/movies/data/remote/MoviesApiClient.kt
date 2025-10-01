package tv.trakt.trakt.core.movies.data.remote

import org.openapitools.client.apis.MoviesApi
import org.openapitools.client.apis.RecommendationsApi
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.networking.ExternalRatingsDto
import tv.trakt.trakt.common.networking.MovieDto
import tv.trakt.trakt.common.networking.RecommendedMovieDto
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
}
