package tv.trakt.trakt.tv.core.sync.data.remote.episodes

import org.openapitools.client.models.PostUsersHiddenRemoveProgress200Response
import tv.trakt.trakt.common.networking.SyncAddHistoryResponseDto
import tv.trakt.trakt.tv.common.model.TraktId
import java.time.ZonedDateTime

internal interface EpisodesSyncRemoteDataSource {
    suspend fun addToHistory(
        episodeId: TraktId,
        watchedAt: ZonedDateTime,
    ): SyncAddHistoryResponseDto

    suspend fun removeFromHistory(episodePlayId: Long): PostUsersHiddenRemoveProgress200Response
}
