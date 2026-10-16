package tv.trakt.trakt.common.core.sync.data.remote.episodes

import org.openapitools.client.models.PostSyncHistoryAdd200Response
import org.openapitools.client.models.PostUsersHiddenRemoveProgress200Response
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.networking.ProgressEpisodeDto
import tv.trakt.trakt.common.networking.SyncAddHistoryResponseDto

interface EpisodesSyncRemoteDataSource {
    suspend fun addToHistory(
        episodeId: TraktId,
        watchedAt: String,
    ): PostSyncHistoryAdd200Response

    suspend fun addToHistory(
        showId: TraktId,
        season: Int,
        episode: Int,
        watchedAt: String,
    ): PostSyncHistoryAdd200Response

    suspend fun addToHistory(
        episodeIds: List<TraktId>,
        watchedAt: String,
    )

    suspend fun addToHistory(episodes: List<Pair<TraktId, String>>)

    suspend fun removeEpisodeFromHistory(episodeId: Int)

    suspend fun removeSeasonFromHistory(seasonId: Int)

    suspend fun removePlayFromHistory(playId: Long)

    suspend fun getPlaybackProgress(
        limit: Int,
        page: Int,
    ): List<ProgressEpisodeDto>
}
