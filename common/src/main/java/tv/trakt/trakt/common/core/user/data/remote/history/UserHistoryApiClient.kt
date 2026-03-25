package tv.trakt.trakt.common.core.user.data.remote.history

import org.openapitools.client.apis.HistoryApi
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.networking.SyncHistoryEpisodeItemDto
import tv.trakt.trakt.common.networking.SyncHistoryMovieItemDto

class UserHistoryApiClient(
    private val historyApi: HistoryApi,
) : UserHistoryRemoteDataSource {
    override suspend fun getEpisodesHistory(
        page: Int,
        limit: Int,
    ): List<SyncHistoryEpisodeItemDto> {
        val response = historyApi.getUsersHistoryEpisodes(
            id = "me",
            extended = "full,cloud9,colors",
            startAt = null,
            endAt = null,
            page = page,
            limit = limit,
        )
        return response.body()
    }

    override suspend fun getMoviesHistory(
        page: Int,
        limit: Int,
    ): List<SyncHistoryMovieItemDto> {
        val response = historyApi.getUsersHistoryMovies(
            id = "me",
            extended = "full,cloud9,colors",
            startAt = null,
            endAt = null,
            page = page,
            limit = limit,
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
        page: Int,
        limit: Int?,
    ): List<SyncHistoryEpisodeItemDto> {
        val response = historyApi.getUsersHistoryShow(
            id = "me",
            itemId = showId.value.toString(),
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
