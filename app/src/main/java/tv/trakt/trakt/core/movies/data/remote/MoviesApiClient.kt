package tv.trakt.trakt.core.movies.data.remote

import org.openapitools.client.apis.MoviesApi
import org.openapitools.client.apis.RecommendationsApi
import tv.trakt.trakt.common.helpers.extensions.getHttpCode
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.model.globalfilter.GlobalFilter
import tv.trakt.trakt.common.networking.CastCrewDto
import tv.trakt.trakt.common.networking.CommentDto
import tv.trakt.trakt.common.networking.ExternalRatingsDto
import tv.trakt.trakt.common.networking.ExtraVideoDto
import tv.trakt.trakt.common.networking.ListDto
import tv.trakt.trakt.common.networking.MovieDto
import tv.trakt.trakt.common.networking.MovieStatsDto
import tv.trakt.trakt.common.networking.RecommendedMovieDto
import tv.trakt.trakt.common.networking.StreamingDto
import tv.trakt.trakt.common.networking.api.v3.V3Api
import tv.trakt.trakt.common.networking.api.v3.model.V3SentimentResponse
import tv.trakt.trakt.core.movies.data.remote.model.AnticipatedMovieDto
import tv.trakt.trakt.core.movies.data.remote.model.TrendingMovieDto
import java.time.Instant

internal class MoviesApiClient(
    private val moviesApi: MoviesApi,
    private val recommendationsApi: RecommendationsApi,
    private val v3Api: V3Api,
) : MoviesRemoteDataSource {
    override suspend fun getTrending(
        page: Int,
        limit: Int,
        filters: GlobalFilter,
        subgenres: List<String>?,
    ): List<TrendingMovieDto> {
        val response = moviesApi.getMoviesTrending(
            extended = "full,streaming_ids,cloud9,colors",
            page = page,
            limit = limit,
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
        filters: GlobalFilter,
    ): List<MovieDto> {
        val response = moviesApi.getMoviesPopular(
            extended = "full,streaming_ids,cloud9,colors",
            page = page,
            limit = limit,
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
        )

        return response.body()
    }

    override suspend fun getRecommended(
        limit: Int,
        filters: GlobalFilter,
    ): List<RecommendedMovieDto> {
        val response = recommendationsApi.getRecommendationsMoviesRecommend(
            extended = "full,streaming_ids,cloud9,colors",
            limit = limit,
            watchWindow = 25,
            watchnow = filters.availability?.joinToString(",") { it.slug },
            genres = filters.genre?.joinToString(",") { it.slug },
            subgenres = filters.subgenre?.joinToString(","),
            years = filters.years?.let { "${it.first}-${it.second}" },
            ratings = filters.rating?.let { "${it.first}-${it.second}" },
            runtimes = filters.runtime?.let { "${it.first}-${it.second}" },
            certifications = filters.certification?.joinToString(",") { it.slug },
            ignoreWatched = true,
            ignoreWatchlisted = filters.hideWatchlist,
            ignoreCollected = null,
            startDate = null,
            endDate = null,
            countries = filters.countries?.joinToString(",") ?: filters.region?.slug,
        )

        return response.body()
    }

    override suspend fun getAnticipated(
        page: Int,
        limit: Int,
        endDate: Instant?,
        filters: GlobalFilter,
    ): List<AnticipatedMovieDto> {
        val response = moviesApi.getMoviesAnticipated(
            extended = "full,streaming_ids,cloud9,colors",
            page = page,
            limit = limit,
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

    override suspend fun getStats(movieId: TraktId): MovieStatsDto {
        val response = moviesApi.getMoviesStats(
            id = movieId.value.toString(),
        )

        return response.body()
    }

    override suspend fun getStreamings(
        movieId: TraktId,
        countryCode: String?,
    ): Map<String, StreamingDto> {
        val response = moviesApi.getMoviesWatchnow(
            country = countryCode ?: "",
            id = movieId.value.toString(),
            links = "direct",
            extended = "streaming_ranks",
        )

        return response.body()
    }

    override suspend fun getJustWatchLink(
        movieId: TraktId,
        countryCode: String?,
    ): String? {
        val response = moviesApi.getMoviesJustwatchLink(
            country = countryCode ?: "",
            id = movieId.value.toString(),
        )
        val body = response.body()
        return body[countryCode]
    }

    override suspend fun getExtras(movieId: TraktId): List<ExtraVideoDto> {
        val response = moviesApi.getMoviesVideos(
            id = movieId.value.toString(),
        )
        return response.body()
    }

    override suspend fun getCastCrew(movieId: TraktId): CastCrewDto {
        return try {
            moviesApi.getMoviesPeople(
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

    override suspend fun getRelated(movieId: TraktId): List<MovieDto> {
        val response = moviesApi.getMoviesRelated(
            id = movieId.value.toString(),
            extended = "full,streaming_ids,cloud9,colors",
            limit = 30,
            page = null,
        )
        return response.body()
    }

    override suspend fun getSentiments(movieId: TraktId): V3SentimentResponse? {
        val response = v3Api.getMovieSentiment(
            movieId = movieId,
        )
        return response
    }

    override suspend fun getComments(
        movieId: TraktId,
        limit: Int,
        sort: String,
    ): List<CommentDto> {
        val response = moviesApi.getMoviesComments(
            id = movieId.value.toString(),
            sort = sort,
            extended = "full,images,vip",
            page = null,
            limit = limit.toString(),
        )

        return response.body()
    }

    override suspend fun getLists(
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
}
