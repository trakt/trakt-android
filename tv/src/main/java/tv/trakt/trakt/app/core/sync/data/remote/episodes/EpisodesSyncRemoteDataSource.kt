package tv.trakt.trakt.app.core.sync.data.remote.episodes

import org.openapitools.client.models.PostUsersHiddenRemoveProgress200Response
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.networking.ProgressEpisodeDto
import tv.trakt.trakt.common.networking.SyncAddHistoryResponseDto

internal interface EpisodesSyncRemoteDataSource {
    suspend fun getPlaybackProgress(
        limit: Int,
        page: Int,
    ): List<ProgressEpisodeDto>

    suspend fun addToHistory(
        episodeId: TraktId,
        watchedAt: String,
    ): SyncAddHistoryResponseDto

    suspend fun removeFromHistory(episodePlayId: Long): PostUsersHiddenRemoveProgress200Response

    suspend fun removeEpisodeFromHistory(episodeId: TraktId)
}
