package tv.trakt.trakt.core.movies.data.remote

import io.ktor.http.HttpStatusCode
import kotlinx.collections.immutable.toImmutableList
import org.openapitools.client.apis.MoviesApi
import org.openapitools.client.apis.RecommendationsApi
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

internal class MoviesApiClient(
    private val moviesApi: MoviesApi,
    private val recommendationsApi: RecommendationsApi,
) : MoviesRemoteDataSource {
    override suspend fun getTrending(
        page: Int,
        limit: Int,
        genres: List<String>?,
        subgenres: List<String>?,
        years: String?,
    ): List<TrendingMovieDto> {
        val response = moviesApi.getMoviesTrending(
            extended = "full,streaming_ids,cloud9,colors",
            page = page,
            limit = limit,
            watchnow = null,
            subgenres = subgenres?.joinToString(","),
            genres = genres?.joinToString(","),
            years = years,
            ratings = null,
            ignoreWatched = null,
            ignoreCollected = null,
            ignoreWatchlisted = null,
            startDate = null,
            endDate = null,
            runtimes = null,
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
        years: String?,
        genres: List<String>?,
        subgenres: List<String>?,
    ): List<MovieDto> {
        val response = moviesApi.getMoviesPopular(
            extended = "full,streaming_ids,cloud9,colors",
            page = page,
            limit = limit,
            years = years,
            watchnow = null,
            subgenres = subgenres?.joinToString(","),
            genres = genres?.joinToString(","),
            ratings = null,
            ignoreWatched = null,
            ignoreCollected = null,
            ignoreWatchlisted = null,
            startDate = null,
            endDate = null,
            runtimes = null,
        )

        return response.body()
    }

    override suspend fun getRecommended(
        limit: Int,
        years: String?,
        genres: List<String>?,
        subgenres: List<String>?,
    ): List<RecommendedMovieDto> {
        val response = recommendationsApi.getRecommendationsMoviesRecommend(
            extended = "full,streaming_ids,cloud9,colors",
            limit = limit,
            ignoreWatched = true,
            ignoreWatchlisted = true,
            ignoreCollected = true,
            watchWindow = 25,
            watchnow = null,
            subgenres = subgenres?.joinToString(","),
            genres = genres?.joinToString(","),
            years = years,
            ratings = null,
            startDate = null,
            endDate = null,
            runtimes = null,
        )

        return response.body()
    }

    override suspend fun getAnticipated(
        page: Int,
        limit: Int,
        endDate: Instant?,
        genres: List<String>?,
        subgenres: List<String>?,
        years: String?,
    ): List<AnticipatedMovieDto> {
        val response = moviesApi.getMoviesAnticipated(
            extended = "full,streaming_ids,cloud9,colors",
            page = page,
            limit = limit,
            watchnow = null,
            subgenres = subgenres?.joinToString(","),
            genres = genres?.joinToString(","),
            years = years,
            ratings = null,
            ignoreWatched = null,
            ignoreCollected = null,
            ignoreWatchlisted = null,
            startDate = null,
            endDate = endDate?.toString(),
            runtimes = null,
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
        )

        if (response.status == HttpStatusCode.NoContent.value) {
            return Sentiments()
        }

        val responseBody = response.body()
        return Sentiments(
            good = responseBody.good
                .map { Sentiments.Sentiment(it.sentiment) }
                .toImmutableList(),
            bad = responseBody.bad
                .map { Sentiments.Sentiment(it.sentiment) }
                .toImmutableList(),
        )
    }

    override suspend fun getComments(
        movieId: TraktId,
        limit: Int,
        sort: String,
    ): List<CommentDto> {
        val response = moviesApi.getMoviesComments(
            id = movieId.value.toString(),
            sort = sort,
            extended = "images",
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
