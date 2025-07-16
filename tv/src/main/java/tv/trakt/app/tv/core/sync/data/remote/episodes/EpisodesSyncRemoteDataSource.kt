package tv.trakt.app.tv.core.sync.data.remote.episodes

import org.openapitools.client.models.PostUsersHiddenRemoveProgress200Response
import tv.trakt.app.tv.common.model.TraktId
import tv.trakt.app.tv.networking.openapi.SyncAddHistoryResponseDto
import java.time.ZonedDateTime

internal interface EpisodesSyncRemoteDataSource {
    suspend fun addToHistory(
        episodeId: TraktId,
        watchedAt: ZonedDateTime,
    ): SyncAddHistoryResponseDto

    suspend fun removeFromHistory(episodePlayId: Long): PostUsersHiddenRemoveProgress200Response
}
