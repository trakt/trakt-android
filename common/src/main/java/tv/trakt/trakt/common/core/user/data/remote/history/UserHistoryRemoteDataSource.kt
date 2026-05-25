package tv.trakt.trakt.common.core.user.data.remote.history

import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.model.globalfilter.GlobalFilter
import tv.trakt.trakt.common.networking.SyncHistoryEpisodeItemDto
import tv.trakt.trakt.common.networking.SyncHistoryMovieItemDto

interface UserHistoryRemoteDataSource {
    suspend fun getEpisodesHistory(
        page: Int = 1,
        limit: Int,
        filters: GlobalFilter?,
    ): List<SyncHistoryEpisodeItemDto>

    suspend fun getMoviesHistory(
        page: Int = 1,
        limit: Int,
        filters: GlobalFilter?,
    ): List<SyncHistoryMovieItemDto>

    suspend fun getMovieHistory(
        movieId: TraktId,
        page: Int = 1,
        limit: Int,
    ): List<SyncHistoryMovieItemDto>

    suspend fun getShowHistory(
        showId: TraktId,
        page: Int = 1,
        limit: Int? = null,
    ): List<SyncHistoryEpisodeItemDto>

    suspend fun getEpisodeHistory(
        episodeId: TraktId,
        page: Int = 1,
        limit: Int?,
    ): List<SyncHistoryEpisodeItemDto>
}
