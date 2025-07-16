package tv.trakt.app.tv.core.sync.data.remote.episodes

import org.openapitools.client.apis.SyncApi
import org.openapitools.client.models.PostSyncHistoryRemoveRequest
import org.openapitools.client.models.PostUsersHiddenRemoveProgress200Response
import org.openapitools.client.models.PostUsersListsListAddRequest
import org.openapitools.client.models.PostUsersListsListAddRequestEpisodesInner
import org.openapitools.client.models.PostUsersListsListAddRequestEpisodesInnerIds
import tv.trakt.app.tv.common.model.TraktId
import tv.trakt.app.tv.networking.openapi.SyncAddHistoryResponseDto
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter.ISO_INSTANT

internal class EpisodesSyncApiClient(
    private val syncApi: SyncApi,
) : EpisodesSyncRemoteDataSource {
    override suspend fun addToHistory(
        episodeId: TraktId,
        watchedAt: ZonedDateTime,
    ): SyncAddHistoryResponseDto {
        val request = PostUsersListsListAddRequest(
            episodes = listOf(
                PostUsersListsListAddRequestEpisodesInner(
                    ids = PostUsersListsListAddRequestEpisodesInnerIds(
                        trakt = episodeId.value,
                        tvdb = -1,
                    ),
                    watchedAt = watchedAt.format(ISO_INSTANT),
                ),
            ),
        )

        val result = syncApi.postSyncHistoryAdd(request)
        return result.body()
    }

    override suspend fun removeFromHistory(episodePlayId: Long): PostUsersHiddenRemoveProgress200Response {
        val request = PostSyncHistoryRemoveRequest(
            ids = listOf(episodePlayId),
        )
        val result = syncApi.postSyncHistoryRemove(request)
        return result.body()
    }
}
