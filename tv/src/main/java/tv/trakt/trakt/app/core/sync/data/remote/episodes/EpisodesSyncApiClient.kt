package tv.trakt.trakt.app.core.sync.data.remote.episodes

import org.openapitools.client.apis.SyncApi
import org.openapitools.client.models.PostSyncHistoryRemoveRequest
import org.openapitools.client.models.PostUsersHiddenRemoveProgress200Response
import org.openapitools.client.models.PostUsersListsListAddRequest
import org.openapitools.client.models.PostUsersListsListAddRequestEpisodesInner
import org.openapitools.client.models.PostUsersListsListAddRequestEpisodesInnerIds
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.networking.SyncAddHistoryResponseDto
import tv.trakt.trakt.common.networking.helpers.CacheMarkerProvider
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter.ISO_INSTANT

internal class EpisodesSyncApiClient(
    private val syncApi: SyncApi,
    private val cacheMarkerProvider: CacheMarkerProvider,
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
        cacheMarkerProvider.invalidate()
        return result.body()
    }

    override suspend fun removeFromHistory(episodePlayId: Long): PostUsersHiddenRemoveProgress200Response {
        val request = PostSyncHistoryRemoveRequest(
            ids = listOf(episodePlayId),
        )
        val result = syncApi.postSyncHistoryRemove(request)
        cacheMarkerProvider.invalidate()
        return result.body()
    }
}
