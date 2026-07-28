package tv.trakt.trakt.app.core.movies.data.remote

import org.openapitools.client.apis.CalendarsApi
import org.openapitools.client.apis.MoviesApi
import org.openapitools.client.apis.RecommendationsApi
import tv.trakt.trakt.app.core.movies.data.remote.model.response.AnticipatedMovieDto
import tv.trakt.trakt.app.core.movies.data.remote.model.response.TrendingMovieDto
import tv.trakt.trakt.common.helpers.extensions.getHttpCode
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.networking.CalendarMediaDto
import tv.trakt.trakt.common.networking.CastCrewDto
import tv.trakt.trakt.common.networking.CommentDto
import tv.trakt.trakt.common.networking.ExternalRatingsDto
import tv.trakt.trakt.common.networking.ExtraVideoDto
import tv.trakt.trakt.common.networking.ListDto
import tv.trakt.trakt.common.networking.MovieCalendarDto
import tv.trakt.trakt.common.networking.RecommendedMovieDto
import tv.trakt.trakt.common.networking.StreamingDto
import java.time.Instant
import java.time.temporal.ChronoUnit.DAYS

internal class MoviesApiClient(
    private val api: MoviesApi,
    private val recommendationsApi: RecommendationsApi,
    private val calendarsApi: CalendarsApi,
) : MoviesRemoteDataSource {
    override suspend fun getTrendingMovies(
        limit: Int,
        page: Int,
    ): List<TrendingMovieDto> {
        val response = api.getMoviesTrending(
            extended = "full,streaming_ids,cloud9,colors",
            limit = limit,
            watchnow = null,
            genres = null,
            subgenres = null,
            years = null,
            ratings = null,
            page = page,
            startDate = null,
            endDate = null,
            ignoreWatched = false,
            ignoreCollected = false,
            ignoreWatchlisted = false,
            runtimes = null,
            countries = null,
            certifications = null,
        )

        return response.body()
            .map {
                TrendingMovieDto(
                    watchers = it.watchers,
                    movie = it.movie,
                )
            }
    }

    override suspend fun getPopularMovies(
        limit: Int,
        page: Int,
        years: Int?,
    ): List<MovieCalendarDto> {
        val response = api.getMoviesPopular(
            extended = "full,streaming_ids,cloud9,colors",
            limit = limit,
            watchnow = null,
            genres = null,
            subgenres = null,
            years = years.toString(),
            ratings = null,
            page = page,
            ignoreWatched = false,
            ignoreCollected = false,
            ignoreWatchlisted = false,
            startDate = null,
            endDate = null,
            runtimes = null,
            countries = null,
            certifications = null,
        )

        return response.body()
    }

    override suspend fun getAnticipatedMovies(
        limit: Int,
        page: Int,
        endDate: Instant,
    ): List<AnticipatedMovieDto> {
        val response = api.getMoviesAnticipated(
            extended = "full,streaming_ids,cloud9,colors",
            limit = limit,
            watchnow = null,
            genres = null,
            subgenres = null,
            years = null,
            ratings = null,
            page = page,
            ignoreWatched = false,
            ignoreCollected = false,
            ignoreWatchlisted = false,
            startDate = null,
            endDate = endDate.toString(),
            runtimes = null,
            countries = null,
            certifications = null,
        )

        return response.body()
            .map {
                AnticipatedMovieDto(
                    listCount = it.listCount,
                    movie = it.movie,
                )
            }
    }

    override suspend fun getRecommendedMovies(
        limit: Int,
        page: Int,
    ): List<RecommendedMovieDto> {
        val response = recommendationsApi.getRecommendationsMoviesRecommend(
            extended = "full,streaming_ids,cloud9,colors",
            limit = limit,
            watchWindow = 25,
            ignoreWatched = true,
            ignoreCollected = true,
            ignoreWatchlisted = true,
            watchnow = null,
            genres = null,
            subgenres = null,
            years = null,
            ratings = null,
            startDate = null,
            endDate = null,
            runtimes = null,
            countries = null,
            certifications = null,
        )

        return response.body()
    }

    override suspend fun getReleases(
        startDate: Instant,
        days: Int,
    ): List<CalendarMediaDto> {
        val response = calendarsApi.getCalendarsReleasesHot(
            extended = "full,images,colors",
            startDate = startDate.truncatedTo(DAYS).toString(),
            days = days,
            watchnow = null,
            genres = null,
            subgenres = null,
            years = null,
            ratings = null,
            startDate2 = null,
            endDate = null,
            runtimes = null,
            countries = null,
            certifications = null,
            type = "movie",
            group = null,
        )

        return response.body()
    }

    override suspend fun getRelatedMovies(movieId: TraktId): List<MovieCalendarDto> {
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
        return try {
            api.getMoviesPeople(
                id = movieId.value.toString(),
                extended = "cloud9",
            ).body()
        } catch (error: Exception) {
            if (error.getHttpCode() == 204) {
                return CastCrewDto()
            }
            throw error
        }
    }

    override suspend fun getMovieComments(movieId: TraktId): List<CommentDto> {
        val response = api.getMoviesComments(
            id = movieId.value.toString(),
            sort = "likes",
            extended = "images",
            page = null,
            limit = 20.toString(),
            language = null,
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

    override suspend fun getMovieDetails(movieId: TraktId): MovieCalendarDto {
        val response = api.getMoviesSummary(
            id = movieId.value.toString(),
            extended = "full,streaming_ids,cloud9,colors",
        )

        return response.body()
    }

    override suspend fun getMovieStreamings(
        movieId: TraktId,
        countryCode: String?,
    ): Map<String, StreamingDto> {
        val response = api.getMoviesWatchnow(
            country = countryCode ?: "",
            id = movieId.value.toString(),
            links = "direct",
            extended = null,
        )

        return response.body()
    }
}
