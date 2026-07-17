package tv.trakt.trakt.core.sync.data.remote.episodes

import org.openapitools.client.apis.SyncApi
import org.openapitools.client.models.PostCheckinStartRequestOneOfOneOfEpisodeIds
import org.openapitools.client.models.PostSyncHistoryAdd200Response
import org.openapitools.client.models.PostSyncHistoryRemoveRequest
import org.openapitools.client.models.PostUsersListsListAddRequest
import org.openapitools.client.models.PostUsersListsListAddRequestEpisodesInner
import org.openapitools.client.models.PostUsersListsListAddRequestEpisodesInnerIds
import org.openapitools.client.models.PostUsersListsListAddRequestSeasonsInner
import org.openapitools.client.models.PostUsersListsListAddRequestSeasonsInnerIds
import org.openapitools.client.models.PostUsersListsListAddRequestShowsInner
import org.openapitools.client.models.PostUsersListsListAddRequestShowsInnerOneOf1SeasonsInner
import org.openapitools.client.models.PostUsersListsListAddRequestShowsInnerOneOf1SeasonsInnerEpisodesInner
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.networking.helpers.CacheMarkerProvider

internal class EpisodesSyncApiClient(
    private val syncApi: SyncApi,
    private val cacheMarker: CacheMarkerProvider,
) : EpisodesSyncRemoteDataSource {
    override suspend fun addToHistory(
        episodeId: TraktId,
        watchedAt: String,
    ): PostSyncHistoryAdd200Response {
        val request = PostUsersListsListAddRequest(
            episodes = listOf(
                PostUsersListsListAddRequestEpisodesInner(
                    ids = PostUsersListsListAddRequestEpisodesInnerIds(
                        trakt = episodeId.value,
                        tvdb = -1,
                    ),
                    watchedAt = watchedAt,
                ),
            ),
        )

        val response = syncApi.postSyncHistoryAdd(request)
        cacheMarker.invalidate()

        return response.body()
    }

    override suspend fun addToHistory(
        showId: TraktId,
        season: Int,
        episode: Int,
        watchedAt: String,
    ): PostSyncHistoryAdd200Response {
        val request = PostUsersListsListAddRequest(
            shows = listOf(
                PostUsersListsListAddRequestShowsInner(
                    seasons = listOf(
                        PostUsersListsListAddRequestShowsInnerOneOf1SeasonsInner(
                            episodes = listOf(
                                PostUsersListsListAddRequestShowsInnerOneOf1SeasonsInnerEpisodesInner(
                                    number = episode,
                                    watchedAt = watchedAt,
                                ),
                            ),
                            number = season,
                            watchedAt = watchedAt,
                        ),
                    ),
                    ids = PostCheckinStartRequestOneOfOneOfEpisodeIds(
                        trakt = showId.value,
                        slug = null,
                        imdb = null,
                        tmdb = null,
                        tvdb = 0,
                    ),
                    title = "",
                    year = 0,
                    watchedAt = watchedAt,
                ),
            ),
        )

        val response = syncApi.postSyncHistoryAdd(request)
        cacheMarker.invalidate()

        return response.body()
    }

    override suspend fun addToHistory(
        episodeIds: List<TraktId>,
        watchedAt: String,
    ) {
        val request = PostUsersListsListAddRequest(
            episodes = episodeIds.map {
                PostUsersListsListAddRequestEpisodesInner(
                    ids = PostUsersListsListAddRequestEpisodesInnerIds(
                        trakt = it.value,
                        tvdb = -1,
                    ),
                    watchedAt = watchedAt,
                )
            },
        )
        syncApi.postSyncHistoryAdd(request)
        cacheMarker.invalidate()
    }

    override suspend fun addToHistory(episodes: List<Pair<TraktId, String>>) {
        val request = PostUsersListsListAddRequest(
            episodes = episodes.map { (episodeId, watchedAt) ->
                PostUsersListsListAddRequestEpisodesInner(
                    ids = PostUsersListsListAddRequestEpisodesInnerIds(
                        trakt = episodeId.value,
                        tvdb = -1,
                    ),
                    watchedAt = watchedAt,
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
