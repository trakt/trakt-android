package tv.trakt.trakt.core.sync.data.remote.episodes

import org.openapitools.client.models.PostSyncHistoryAdd200Response
import tv.trakt.trakt.common.model.TraktId

internal interface EpisodesSyncRemoteDataSource {
    suspend fun addToHistory(
        episodeId: TraktId,
        watchedAt: String,
    ): PostSyncHistoryAdd200Response

    suspend fun addToHistory(
        episodeIds: List<TraktId>,
        watchedAt: String,
    )

    suspend fun removeEpisodeFromHistory(episodeId: Int)

    suspend fun removeSeasonFromHistory(seasonId: Int)

    suspend fun removePlayFromHistory(playId: Long)
}
