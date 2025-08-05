package tv.trakt.trakt.core.movies.data.remote

import org.openapitools.client.apis.MoviesApi
import tv.trakt.trakt.common.networking.MovieDto
import tv.trakt.trakt.core.movies.data.remote.model.AnticipatedMovieDto
import tv.trakt.trakt.core.movies.data.remote.model.TrendingMovieDto

internal class MoviesApiClient(
    private val api: MoviesApi,
) : MoviesRemoteDataSource {
    override suspend fun getTrending(limit: Int): List<TrendingMovieDto> {
        val response = api.getMoviesTrending(
            extended = "full,streaming_ids,cloud9,colors",
            limit = limit,
            watchnow = null,
            genres = null,
            years = null,
            ratings = null,
            page = null,
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

    override suspend fun getHot(limit: Int): List<TrendingMovieDto> {
        val response = api.getMoviesHot(
            extended = "full,streaming_ids,cloud9,colors",
            limit = limit,
            watchnow = null,
            genres = null,
            years = null,
            ratings = null,
            page = null,
            ignoreWatched = null,
            ignoreCollected = null,
            ignoreWatchlisted = null,
            startDate = "lastmonth",
            endDate = null,
        )

        return response.body()
            .map {
                TrendingMovieDto(
                    watchers = it.listCount,
                    movie = it.movie,
                )
            }
    }

    override suspend fun getPopular(limit: Int): List<MovieDto> {
        val response = api.getMoviesPopular(
            extended = "full,streaming_ids,cloud9,colors",
            limit = limit,
            watchnow = null,
            genres = null,
            years = null,
            ratings = null,
            page = null,
            ignoreWatched = null,
            ignoreCollected = null,
            ignoreWatchlisted = null,
            startDate = null,
            endDate = null,
        )

        return response.body()
    }

    override suspend fun getAnticipated(limit: Int): List<AnticipatedMovieDto> {
        val response = api.getMoviesAnticipated(
            extended = "full,streaming_ids,cloud9,colors",
            limit = limit,
            watchnow = null,
            genres = null,
            years = null,
            ratings = null,
            page = null,
            ignoreWatched = null,
            ignoreCollected = null,
            ignoreWatchlisted = null,
            startDate = null,
            endDate = null,
        )

        return response.body()
            .map {
                AnticipatedMovieDto(
                    listCount = it.listCount,
                    movie = it.movie,
                )
            }
    }
}
