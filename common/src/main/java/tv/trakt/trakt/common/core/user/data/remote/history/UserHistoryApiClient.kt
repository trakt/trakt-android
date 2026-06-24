package tv.trakt.trakt.common.core.user.data.remote.history

import org.openapitools.client.apis.HistoryApi
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.model.globalfilter.GlobalFilter
import tv.trakt.trakt.common.model.pagination.Pagination
import tv.trakt.trakt.common.networking.SyncHistoryEpisodeItemDto
import tv.trakt.trakt.common.networking.SyncHistoryMovieItemDto
import kotlin.time.Instant

class UserHistoryApiClient(
    private val historyApi: HistoryApi,
) : UserHistoryRemoteDataSource {
    override suspend fun getEpisodesHistory(
        page: Int,
        limit: Int,
        filters: GlobalFilter?,
        from: Instant?,
        to: Instant?,
        userId: String,
    ): List<SyncHistoryEpisodeItemDto> {
        val response = historyApi.getUsersHistoryEpisodes(
            id = userId,
            extended = "full,cloud9,colors",
            startAt = from?.toString(),
            endAt = to?.toString(),
            page = page,
            limit = limit,
            watchnow = filters?.availability?.joinToString(",") { it.slug },
            genres = filters?.genre?.joinToString(",") { it.slug },
            subgenres = filters?.subgenre?.joinToString(","),
            years = filters?.years?.let { "${it.first}-${it.second}" },
            ratings = filters?.rating?.let { "${it.first}-${it.second}" },
            startDate = null,
            endDate = null,
            runtimes = filters?.runtime?.let { "${it.first}-${it.second}" },
            countries = filters?.countries?.joinToString(",") ?: filters?.region?.slug,
            certifications = filters?.certification?.joinToString(",") { it.slug },
        )
        return response.body()
    }

    override suspend fun getMoviesHistory(
        page: Int,
        limit: Int,
        filters: GlobalFilter?,
        from: Instant?,
        to: Instant?,
        userId: String,
    ): List<SyncHistoryMovieItemDto> {
        val response = historyApi.getUsersHistoryMovies(
            id = userId,
            extended = "full,cloud9,colors",
            startAt = from?.toString(),
            endAt = to?.toString(),
            page = page,
            limit = limit,
            watchnow = filters?.availability?.joinToString(",") { it.slug },
            genres = filters?.genre?.joinToString(",") { it.slug },
            subgenres = filters?.subgenre?.joinToString(","),
            years = filters?.years?.let { "${it.first}-${it.second}" },
            ratings = filters?.rating?.let { "${it.first}-${it.second}" },
            startDate = null,
            endDate = null,
            runtimes = filters?.runtime?.let { "${it.first}-${it.second}" },
            countries = filters?.countries?.joinToString(",") ?: filters?.region?.slug,
            certifications = filters?.certification?.joinToString(",") { it.slug },
        )
        return response.body()
    }

    override suspend fun getMovieHistory(
        movieId: TraktId,
        page: Int,
        limit: Int,
    ): List<SyncHistoryMovieItemDto> {
        val response = historyApi.getUsersHistoryMovie(
            id = "me",
            itemId = movieId.value.toString(),
            extended = "full,cloud9,colors",
            startAt = null,
            endAt = null,
            page = page,
            limit = limit,
        )
        return response.body()
    }

    override suspend fun getShowHistory(
        showId: TraktId,
        pagination: Pagination,
    ): List<SyncHistoryEpisodeItemDto> {
        val response = historyApi.getUsersHistoryShow(
            id = "me",
            itemId = showId.value.toString(),
            extended = "full,cloud9,colors",
            startAt = null,
            endAt = null,
            page = pagination.page,
            limit = pagination.limit,
        )
        return response.body()
    }

    override suspend fun getEpisodeHistory(
        episodeId: TraktId,
        page: Int,
        limit: Int?,
    ): List<SyncHistoryEpisodeItemDto> {
        val response = historyApi.getUsersHistoryEpisode(
            id = "me",
            itemId = episodeId.value.toString(),
            extended = "full,cloud9,colors",
            startAt = null,
            endAt = null,
            page = page,
            limit = when {
                limit == null -> 99_999
                else -> limit
            },
        )
        return response.body()
    }
}
