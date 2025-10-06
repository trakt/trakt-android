package tv.trakt.trakt.core.sync.data.remote.episodes

import org.openapitools.client.apis.SyncApi
import org.openapitools.client.models.PostUsersListsListAddRequest
import org.openapitools.client.models.PostUsersListsListAddRequestEpisodesInner
import org.openapitools.client.models.PostUsersListsListAddRequestEpisodesInnerIds
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.networking.helpers.CacheMarkerProvider
import java.time.Instant

internal class EpisodesSyncApiClient(
    private val syncApi: SyncApi,
    private val cacheMarker: CacheMarkerProvider,
) : EpisodesSyncRemoteDataSource {
    override suspend fun addToHistory(
        episodeId: TraktId,
        watchedAt: Instant,
    ) {
        val request = PostUsersListsListAddRequest(
            episodes = listOf(
                PostUsersListsListAddRequestEpisodesInner(
                    ids = PostUsersListsListAddRequestEpisodesInnerIds(
                        trakt = episodeId.value,
                        tvdb = -1,
                    ),
                    watchedAt = watchedAt.toString(),
                ),
            ),
        )
        syncApi.postSyncHistoryAdd(request)
        cacheMarker.invalidate()
    }
}
