package tv.trakt.trakt.common.data.remote

import org.openapitools.client.apis.CalendarsApi
import org.openapitools.client.apis.MoviesApi
import org.openapitools.client.apis.RecommendationsApi
import tv.trakt.trakt.common.data.dto.AnticipatedMovieDto
import tv.trakt.trakt.common.data.dto.TrendingMovieDto
import tv.trakt.trakt.common.helpers.extensions.getHttpCode
import tv.trakt.trakt.common.model.SlugId
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.model.globalfilter.GlobalFilter
import tv.trakt.trakt.common.networking.CalendarMediaDto
import tv.trakt.trakt.common.networking.CastCrewDto
import tv.trakt.trakt.common.networking.CommentDto
import tv.trakt.trakt.common.networking.ExternalMovieRatingsDto
import tv.trakt.trakt.common.networking.ExtraVideoDto
import tv.trakt.trakt.common.networking.ListDto
import tv.trakt.trakt.common.networking.MovieCalendarDto
import tv.trakt.trakt.common.networking.MovieStatsDto
import tv.trakt.trakt.common.networking.RecommendedMovieDto
import tv.trakt.trakt.common.networking.api.v3.V3Api
import tv.trakt.trakt.common.networking.api.v3.model.V3MovieRecommendationResponse
import tv.trakt.trakt.common.networking.api.v3.model.V3RecommendationsRequest
import tv.trakt.trakt.common.networking.api.v3.model.V3SentimentResponse
import java.time.Instant
import java.time.temporal.ChronoUnit.DAYS

class MoviesRemoteSource(
    private val moviesApi: MoviesApi,
    private val calendarsApi: CalendarsApi,
    private val v3Api: V3Api,
    private val recommendationsApi: RecommendationsApi,
) {
    suspend fun getTrending(
        page: Int = 1,
        limit: Int,
        filters: GlobalFilter = GlobalFilter.Default,
        subgenres: List<String>? = null,
        theme: String? = null,
    ): List<TrendingMovieDto> {
        val response = moviesApi.getMoviesTrending(
            extended = "full,streaming_ids,images,colors",
            page = page,
            limit = limit,
            theme = theme,
            watchnow = filters.availability?.joinToString(",") { it.slug },
            genres = filters.genre?.joinToString(",") { it.slug },
            subgenres = subgenres?.joinToString(","),
            years = filters.years?.let { "${it.first}-${it.second}" },
            ratings = filters.rating?.let { "${it.first}-${it.second}" },
            runtimes = filters.runtime?.let { "${it.first}-${it.second}" },
            certifications = filters.certification?.joinToString(",") { it.slug },
            ignoreWatched = filters.hideWatched,
            ignoreWatchlisted = filters.hideWatchlist,
            ignoreCollected = null,
            startDate = null,
            endDate = null,
            countries = filters.countries?.joinToString(",") ?: filters.region?.slug,
            statuses = filters.statuses?.joinToString(",") { it.slug },
        )

        return response.body()
            .map {
                TrendingMovieDto(
                    watchers = it.watchers,
                    movie = it.movie,
                )
            }
    }

    suspend fun getPopular(
        page: Int = 1,
        limit: Int,
        filters: GlobalFilter = GlobalFilter.Default,
        theme: String? = null,
    ): List<MovieCalendarDto> {
        val response = moviesApi.getMoviesPopular(
            extended = "full,streaming_ids,images,colors",
            page = page,
            limit = limit,
            theme = theme,
            watchnow = filters.availability?.joinToString(",") { it.slug },
            genres = filters.genre?.joinToString(",") { it.slug },
            subgenres = filters.subgenre?.joinToString(","),
            years = filters.years?.let { "${it.first}-${it.second}" },
            ratings = filters.rating?.let { "${it.first}-${it.second}" },
            runtimes = filters.runtime?.let { "${it.first}-${it.second}" },
            certifications = filters.certification?.joinToString(",") { it.slug },
            ignoreWatched = filters.hideWatched,
            ignoreWatchlisted = filters.hideWatchlist,
            ignoreCollected = null,
            startDate = null,
            endDate = null,
            countries = filters.countries?.joinToString(",") ?: filters.region?.slug,
            statuses = filters.statuses?.joinToString(",") { it.slug },
        )

        return response.body()
    }

    suspend fun getRecommended(
        limit: Int,
        filters: GlobalFilter = GlobalFilter.Default,
    ): List<V3MovieRecommendationResponse> {
        return v3Api.getMovieRecommendations(
            V3RecommendationsRequest(
                limit = limit,
                extended = "full,streaming_ids,images,colors",
                watchWindow = 25,
                watchnow = filters.availability?.joinToString(",") { it.slug },
                genres = filters.genre?.joinToString(",") { it.slug },
                subgenres = filters.subgenre?.joinToString(","),
                years = filters.years?.let { "${it.first}-${it.second}" },
                ratings = filters.rating?.let { "${it.first}-${it.second}" },
                runtimes = filters.runtime?.let { "${it.first}-${it.second}" },
                certifications = filters.certification?.joinToString(",") { it.slug },
                countries = filters.countries?.joinToString(",") ?: filters.region?.slug,
                statuses = filters.statuses?.joinToString(",") { it.slug },
                ignoreWatched = true,
                ignoreWatchlisted = filters.hideWatchlist,
                ignoreCollected = true,
            ),
        )
    }

    suspend fun getRecommendedV2(limit: Int): List<RecommendedMovieDto> {
        val response = recommendationsApi.getRecommendationsMoviesRecommend(
            extended = "full,streaming_ids,images,colors",
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

    suspend fun getAnticipated(
        page: Int = 1,
        limit: Int,
        endDate: Instant? = null,
        filters: GlobalFilter = GlobalFilter.Default,
        theme: String? = null,
    ): List<AnticipatedMovieDto> {
        val response = moviesApi.getMoviesAnticipated(
            extended = "full,streaming_ids,images,colors",
            page = page,
            limit = limit,
            theme = theme,
            watchnow = filters.availability?.joinToString(",") { it.slug },
            genres = filters.genre?.joinToString(",") { it.slug },
            subgenres = filters.subgenre?.joinToString(","),
            years = filters.years?.let { "${it.first}-${it.second}" },
            ratings = filters.rating?.let { "${it.first}-${it.second}" },
            runtimes = filters.runtime?.let { "${it.first}-${it.second}" },
            certifications = filters.certification?.joinToString(",") { it.slug },
            ignoreWatched = filters.hideWatched,
            ignoreWatchlisted = filters.hideWatchlist,
            ignoreCollected = null,
            startDate = null,
            endDate = endDate?.toString(),
            countries = filters.countries?.joinToString(",") ?: filters.region?.slug,
            statuses = filters.statuses?.joinToString(",") { it.slug },
        )

        return response.body()
            .map {
                AnticipatedMovieDto(
                    listCount = it.listCount,
                    movie = it.movie,
                )
            }
    }

    suspend fun getReleases(
        startDate: Instant,
        days: Int,
        filters: GlobalFilter = GlobalFilter.Default,
    ): List<CalendarMediaDto> {
        val response = calendarsApi.getCalendarsReleasesHot(
            extended = "full,images,colors",
            startDate = startDate.truncatedTo(DAYS).toString(),
            endDate = null,
            days = days,
            watchnow = filters.availability?.joinToString(",") { it.slug },
            genres = filters.genre?.joinToString(",") { it.slug },
            subgenres = filters.subgenre?.joinToString(","),
            years = filters.years?.let { "${it.first}-${it.second}" },
            ratings = filters.rating?.let { "${it.first}-${it.second}" },
            runtimes = filters.runtime?.let { "${it.first}-${it.second}" },
            certifications = filters.certification?.joinToString(",") { it.slug },
            countries = filters.countries?.joinToString(",") ?: filters.region?.slug,
            statuses = filters.statuses?.joinToString(",") { it.slug },
            startDate2 = null,
            type = "movie",
            group = null,
        )

        return response.body()
    }

    suspend fun getDetails(movieId: TraktId): MovieCalendarDto {
        return getMovieSummary(movieId.value.toString())
    }

    suspend fun getDetails(movieSlug: SlugId): MovieCalendarDto {
        return getMovieSummary(movieSlug.value)
    }

    private suspend fun getMovieSummary(id: String): MovieCalendarDto {
        val response = moviesApi.getMoviesSummary(
            id = id,
            extended = "full,streaming_ids,images,colors",
        )

        return response.body()
    }

    suspend fun getExternalRatings(movieId: TraktId): ExternalMovieRatingsDto {
        val response = moviesApi.getMoviesRatings(
            id = movieId.value.toString(),
            extended = "all",
        )

        return response.body()
    }

    suspend fun getStudios(movieId: TraktId): List<String> {
        val response = moviesApi.getMoviesStudios(
            id = movieId.value.toString(),
        )

        return response.body().map { it.name }
    }

    suspend fun getStats(movieId: TraktId): MovieStatsDto {
        val response = moviesApi.getMoviesStats(
            id = movieId.value.toString(),
        )

        return response.body()
    }

    suspend fun getExtras(movieId: TraktId): List<ExtraVideoDto> {
        val response = moviesApi.getMoviesVideos(
            id = movieId.value.toString(),
        )
        return response.body()
    }

    suspend fun getCastCrew(
        movieId: TraktId,
        extended: String = "images,full,markdown",
    ): CastCrewDto {
        return try {
            moviesApi.getMoviesPeople(
                id = movieId.value.toString(),
                extended = extended,
            ).body()
        } catch (error: Exception) {
            if (error.getHttpCode() == 204) {
                return CastCrewDto()
            }
            throw error
        }
    }

    suspend fun getRelated(
        movieId: TraktId,
        limit: Int = 30,
    ): List<MovieCalendarDto> {
        val response = moviesApi.getMoviesRelated(
            id = movieId.value.toString(),
            extended = "full,streaming_ids,images,colors",
            limit = limit,
            page = null,
        )
        return response.body()
    }

    suspend fun getSentiments(movieId: TraktId): V3SentimentResponse? {
        return try {
            v3Api.getMovieSentiment(movieId)
        } catch (error: Exception) {
            // 404 means no sentiment data is available for this movie, so we return null.
            if (error.getHttpCode() == 404) {
                return null
            }
            throw error
        }
    }

    suspend fun getComments(
        movieId: TraktId,
        limit: Int,
        sort: String,
        language: String? = null,
        extended: String = "full,images,vip",
    ): List<CommentDto> {
        val response = moviesApi.getMoviesComments(
            id = movieId.value.toString(),
            sort = sort,
            extended = extended,
            page = null,
            limit = limit.toString(),
            language = language,
        )

        return response.body()
    }

    suspend fun getLists(
        movieId: TraktId,
        type: String,
        limit: Int,
    ): List<ListDto> {
        val response = moviesApi.getMoviesLists(
            id = movieId.value.toString(),
            type = type,
            extended = "images",
            page = null,
            limit = limit,
            sort = "popular",
        )

        return response.body()
    }

    suspend fun postReport(
        movieId: TraktId,
        reason: String,
        message: String,
    ) {
        moviesApi.postMoviesReport(
            id = movieId.value.toString(),
            postMoviesReportRequest = createReportRequest(reason, message),
        )
    }
}
