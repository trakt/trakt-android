package tv.trakt.trakt.common.core.user.data.remote.history

import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.model.globalfilter.GlobalFilter
import tv.trakt.trakt.common.model.pagination.Pagination
import tv.trakt.trakt.common.networking.SyncHistoryEpisodeItemDto
import tv.trakt.trakt.common.networking.SyncHistoryItemDto
import tv.trakt.trakt.common.networking.SyncHistoryMovieItemDto
import kotlin.time.Instant

interface UserHistoryRemoteDataSource {
    suspend fun getMediaHistory(
        page: Int = 1,
        limit: Int,
        filters: GlobalFilter?,
        from: Instant? = null,
        to: Instant? = null,
        userId: String = "me",
    ): List<SyncHistoryItemDto>

    suspend fun getEpisodesHistory(
        page: Int = 1,
        limit: Int,
        filters: GlobalFilter?,
        from: Instant? = null,
        to: Instant? = null,
        userId: String = "me",
    ): List<SyncHistoryEpisodeItemDto>

    suspend fun getMoviesHistory(
        page: Int = 1,
        limit: Int,
        filters: GlobalFilter?,
        from: Instant? = null,
        to: Instant? = null,
        userId: String = "me",
    ): List<SyncHistoryMovieItemDto>

    suspend fun getMovieHistory(
        movieId: TraktId,
        page: Int = 1,
        limit: Int,
    ): List<SyncHistoryMovieItemDto>

    suspend fun getShowHistory(
        showId: TraktId,
        pagination: Pagination,
    ): List<SyncHistoryEpisodeItemDto>

    suspend fun getEpisodeHistory(
        episodeId: TraktId,
        page: Int = 1,
        limit: Int?,
    ): List<SyncHistoryEpisodeItemDto>
}
