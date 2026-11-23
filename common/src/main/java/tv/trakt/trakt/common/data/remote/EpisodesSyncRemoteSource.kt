package tv.trakt.trakt.common.data.remote

import org.openapitools.client.apis.SyncApi
import org.openapitools.client.models.PostSyncHistoryAdd200Response
import org.openapitools.client.models.PostSyncHistoryAddRequest
import org.openapitools.client.models.PostSyncHistoryAddRequestEpisodesInner
import org.openapitools.client.models.PostSyncHistoryAddRequestSeasonsInner
import org.openapitools.client.models.PostSyncHistoryAddRequestShowsInner
import org.openapitools.client.models.PostSyncHistoryAddRequestShowsInnerSeasonsInner
import org.openapitools.client.models.PostSyncHistoryAddRequestShowsInnerSeasonsInnerEpisodesInner
import org.openapitools.client.models.PostSyncHistoryRemoveRequest
import org.openapitools.client.models.PostUsersListsListAddRequestEpisodesInnerIds
import org.openapitools.client.models.PostUsersListsListAddRequestSeasonsInnerIds
import org.openapitools.client.models.PostUsersListsListAddRequestShowsInnerIds
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.networking.ProgressEpisodeDto
import tv.trakt.trakt.common.networking.api.scrobble.ScrobbleExtrasApi
import tv.trakt.trakt.common.networking.helpers.CacheMarkerProvider

class EpisodesSyncRemoteSource(
    private val syncApi: SyncApi,
    private val cacheMarker: CacheMarkerProvider,
    private val scrobbleExtrasApi: ScrobbleExtrasApi,
) {
    suspend fun addToHistory(
        episodeId: TraktId,
        watchedAt: String,
    ): PostSyncHistoryAdd200Response {
        val request = PostSyncHistoryAddRequest(
            episodes = listOf(
                PostSyncHistoryAddRequestEpisodesInner(
                    ids = PostUsersListsListAddRequestEpisodesInnerIds(
                        trakt = episodeId.value,
                        tvdb = -1,
                        imdb = null,
                        tmdb = null,
                    ),
                    watchedAt = watchedAt,
                ),
            ),
        )

        val response = syncApi.postSyncHistoryAdd(request)
        cacheMarker.invalidate()

        return response.body()
    }

    suspend fun addToHistory(
        showId: TraktId,
        season: Int,
        episode: Int,
        watchedAt: String,
    ): PostSyncHistoryAdd200Response {
        val request = PostSyncHistoryAddRequest(
            shows = listOf(
                PostSyncHistoryAddRequestShowsInner(
                    seasons = listOf(
                        PostSyncHistoryAddRequestShowsInnerSeasonsInner(
                            episodes = listOf(
                                PostSyncHistoryAddRequestShowsInnerSeasonsInnerEpisodesInner(
                                    number = episode,
                                    watchedAt = watchedAt,
                                ),
                            ),
                            number = season,
                            watchedAt = watchedAt,
                        ),
                    ),
                    ids = PostUsersListsListAddRequestShowsInnerIds(
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

    suspend fun addToHistory(
        episodeIds: List<TraktId>,
        watchedAt: String,
    ) {
        val request = PostSyncHistoryAddRequest(
            episodes = episodeIds.map {
                PostSyncHistoryAddRequestEpisodesInner(
                    ids = PostUsersListsListAddRequestEpisodesInnerIds(
                        trakt = it.value,
                        tvdb = -1,
                        imdb = null,
                        tmdb = null,
                    ),
                    watchedAt = watchedAt,
                )
            },
        )
        syncApi.postSyncHistoryAdd(request)
        cacheMarker.invalidate()
    }

    suspend fun addToHistory(episodes: List<Pair<TraktId, String>>) {
        val request = PostSyncHistoryAddRequest(
            episodes = episodes.map { (episodeId, watchedAt) ->
                PostSyncHistoryAddRequestEpisodesInner(
                    ids = PostUsersListsListAddRequestEpisodesInnerIds(
                        trakt = episodeId.value,
                        tvdb = -1,
                        imdb = null,
                        tmdb = null,
                    ),
                    watchedAt = watchedAt,
                )
            },
        )
        syncApi.postSyncHistoryAdd(request)
        cacheMarker.invalidate()
    }

    suspend fun removePlayFromHistory(playId: Long) {
        val request = PostSyncHistoryRemoveRequest(
            ids = listOf(playId),
        )
        syncApi.postSyncHistoryRemove(request)
        cacheMarker.invalidate()
    }

    suspend fun removeEpisodeFromHistory(episodeId: Int) {
        val request = PostSyncHistoryRemoveRequest(
            episodes = listOf(
                PostSyncHistoryAddRequestEpisodesInner(
                    ids = PostUsersListsListAddRequestEpisodesInnerIds(
                        trakt = episodeId,
                        tvdb = -1,
                        imdb = null,
                        tmdb = null,
                    ),
                ),
            ),
        )
        syncApi.postSyncHistoryRemove(request)
        cacheMarker.invalidate()
    }

    suspend fun removeSeasonFromHistory(seasonId: Int) {
        val request = PostSyncHistoryRemoveRequest(
            seasons = listOf(
                PostSyncHistoryAddRequestSeasonsInner(
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

    suspend fun getPlaybackProgress(
        limit: Int,
        page: Int,
    ): List<ProgressEpisodeDto> {
        val response = scrobbleExtrasApi.getSyncProgressEpisodes(
            page = page,
            limit = limit,
        )
        return response
    }
}
