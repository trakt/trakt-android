package tv.trakt.trakt.core.sync.data.remote.episodes

import org.openapitools.client.apis.SyncApi
import org.openapitools.client.models.PostSyncHistoryRemoveRequest
import org.openapitools.client.models.PostUsersListsListAddRequest
import org.openapitools.client.models.PostUsersListsListAddRequestEpisodesInner
import org.openapitools.client.models.PostUsersListsListAddRequestEpisodesInnerIds
import org.openapitools.client.models.PostUsersListsListAddRequestSeasonsInner
import org.openapitools.client.models.PostUsersListsListAddRequestSeasonsInnerIds
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

    override suspend fun addToHistory(
        episodeIds: List<TraktId>,
        watchedAt: Instant,
    ) {
        val request = PostUsersListsListAddRequest(
            episodes = episodeIds.map {
                PostUsersListsListAddRequestEpisodesInner(
                    ids = PostUsersListsListAddRequestEpisodesInnerIds(
                        trakt = it.value,
                        tvdb = -1,
                    ),
                    watchedAt = watchedAt.toString(),
                )
            },
        )
        syncApi.postSyncHistoryAdd(request)
        cacheMarker.invalidate()
    }

    override suspend fun removePlayFromHistory(playId: Long) {
        val request = PostSyncHistoryRemoveRequest(
            ids = listOf(playId),
        )
        syncApi.postSyncHistoryRemove(request)
        cacheMarker.invalidate()
    }

    override suspend fun removeEpisodeFromHistory(episodeId: Int) {
        val request = PostSyncHistoryRemoveRequest(
            episodes = listOf(
                PostUsersListsListAddRequestEpisodesInner(
                    ids = PostUsersListsListAddRequestEpisodesInnerIds(
                        trakt = episodeId,
                        tvdb = -1,
                    ),
                ),
            ),
        )
        syncApi.postSyncHistoryRemove(request)
        cacheMarker.invalidate()
    }

    override suspend fun removeSeasonFromHistory(seasonId: Int) {
        val request = PostSyncHistoryRemoveRequest(
            seasons = listOf(
                PostUsersListsListAddRequestSeasonsInner(
                    ids = PostUsersListsListAddRequestSeasonsInnerIds(
                        trakt = seasonId,
                        tmdb = null,
                        tvdb = -1,
                    ),
                ),
            ),
        )
        syncApi.postSyncHistoryRemove(request)
        cacheMarker.invalidate()
    }
}
