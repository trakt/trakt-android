package tv.trakt.trakt.app.core.movies.data.remote

import org.openapitools.client.apis.MoviesApi
import org.openapitools.client.apis.RecommendationsApi
import tv.trakt.trakt.app.core.movies.data.remote.model.response.AnticipatedMovieDto
import tv.trakt.trakt.app.core.movies.data.remote.model.response.TrendingMovieDto
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.networking.CastCrewDto
import tv.trakt.trakt.common.networking.CommentDto
import tv.trakt.trakt.common.networking.ExternalRatingsDto
import tv.trakt.trakt.common.networking.ExtraVideoDto
import tv.trakt.trakt.common.networking.ListDto
import tv.trakt.trakt.common.networking.MovieDto
import tv.trakt.trakt.common.networking.RecommendedMovieDto
import tv.trakt.trakt.common.networking.StreamingDto

internal class MoviesApiClient(
    private val api: MoviesApi,
    private val recommendationsApi: RecommendationsApi,
) : MoviesRemoteDataSource {
    override suspend fun getTrendingMovies(): List<TrendingMovieDto> {
        val response = api.getMoviesTrending(
            extended = "full,streaming_ids,cloud9,colors",
            limit = 30,
            watchnow = null,
            genres = null,
            years = null,
            ratings = null,
            page = null,
            startDate = null,
            endDate = null,
            ignoreWatched = false,
            ignoreCollected = false,
            ignoreWatchlisted = false,
        )

        return response.body()
            .map {
                TrendingMovieDto(
                    watchers = it.watchers,
                    movie = it.movie,
                )
            }
    }

    override suspend fun getMonthlyHotMovies(): List<TrendingMovieDto> {
        val response = api.getMoviesHot(
            extended = "full,streaming_ids,cloud9,colors",
            limit = 30,
            watchnow = null,
            genres = null,
            years = null,
            ratings = null,
            page = null,
            startDate = "lastmonth",
            endDate = null,
            ignoreWatched = false,
            ignoreCollected = false,
            ignoreWatchlisted = false,
        )

        return response.body()
            .map {
                TrendingMovieDto(
                    watchers = it.listCount,
                    movie = it.movie,
                )
            }
    }

    override suspend fun getPopularMovies(): List<MovieDto> {
        val response = api.getMoviesPopular(
            extended = "full,streaming_ids,cloud9,colors",
            limit = 30,
            watchnow = null,
            genres = null,
            years = null,
            ratings = null,
            page = null,
            ignoreWatched = false,
            ignoreCollected = false,
            ignoreWatchlisted = false,
            startDate = null,
            endDate = null,
        )

        return response.body()
    }

    override suspend fun getAnticipatedMovies(): List<AnticipatedMovieDto> {
        val response = api.getMoviesAnticipated(
            extended = "full,streaming_ids,cloud9,colors",
            limit = 30,
            watchnow = null,
            genres = null,
            years = null,
            ratings = null,
            page = null,
            ignoreWatched = false,
            ignoreCollected = false,
            ignoreWatchlisted = false,
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

    override suspend fun getRecommendedMovies(): List<RecommendedMovieDto> {
        val response = recommendationsApi.getRecommendationsMoviesRecommend(
            extended = "full,streaming_ids,cloud9,colors",
            limit = 30,
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

    override suspend fun getRelatedMovies(movieId: TraktId): List<MovieDto> {
        val response = api.getMoviesRelated(
            id = movieId.value.toString(),
            extended = "full,streaming_ids,cloud9,colors",
            limit = 20,
            page = null,
        )

        return response.body()
    }

    override suspend fun getMovieExternalRatings(movieId: TraktId): ExternalRatingsDto {
        val response = api.getMoviesRatings(
            id = movieId.value.toString(),
            extended = "all",
        )

        return response.body()
    }

    override suspend fun getMovieExtras(movieId: TraktId): List<ExtraVideoDto> {
        val response = api.getMoviesVideos(
            id = movieId.value.toString(),
        )
        return response.body()
    }

    override suspend fun getMovieCastCrew(movieId: TraktId): CastCrewDto {
        val response = api.getMoviesPeople(
            id = movieId.value.toString(),
            extended = "cloud9",
        )
        return response.body()
    }

    override suspend fun getMovieComments(movieId: TraktId): List<CommentDto> {
        val response = api.getMoviesComments(
            id = movieId.value.toString(),
            sort = "likes",
            extended = "images",
            page = null,
            limit = 20.toString(),
        )

        return response.body()
    }

    override suspend fun getMovieLists(
        movieId: TraktId,
        type: String,
        limit: Int,
    ): List<ListDto> {
        val response = api.getMoviesLists(
            id = movieId.value.toString(),
            type = type,
            extended = "images",
            page = null,
            limit = limit,
            sort = "popular",
        )

        return response.body()
    }

    override suspend fun getMovieDetails(movieId: TraktId): MovieDto {
        val response = api.getMoviesSummary(
            id = movieId.value.toString(),
            extended = "full,streaming_ids,cloud9",
        )

        return response.body()
    }

    override suspend fun getMovieStreamings(
        movieId: TraktId,
        countryCode: String,
    ): Map<String, StreamingDto> {
        val response = api.getMoviesWatchnow(
            country = countryCode,
            id = movieId.value.toString(),
            links = "direct",
        )

        return response.body()
    }
}
