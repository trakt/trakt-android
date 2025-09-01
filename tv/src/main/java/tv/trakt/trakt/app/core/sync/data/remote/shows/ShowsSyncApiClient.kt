package tv.trakt.trakt.app.core.sync.data.remote.shows

import org.openapitools.client.apis.CollectionApi
import org.openapitools.client.apis.SyncApi
import org.openapitools.client.apis.UsersApi
import org.openapitools.client.apis.WatchedApi
import org.openapitools.client.models.PostUsersListsListAddRequest
import org.openapitools.client.models.PostUsersListsListAddRequestShowsInner
import org.openapitools.client.models.PostUsersListsListAddRequestShowsInnerOneOfIds
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.model.toTraktId
import tv.trakt.trakt.common.networking.ProgressShowDto
import tv.trakt.trakt.common.networking.SyncAddHistoryResponseDto
import tv.trakt.trakt.common.networking.WatchedShowDto
import tv.trakt.trakt.common.networking.WatchlistShowDto
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter.ISO_INSTANT

internal class ShowsSyncApiClient(
    private val usersApi: UsersApi,
    private val syncApi: SyncApi,
    private val watchedApi: WatchedApi,
    private val collectionApi: CollectionApi,
) : ShowsSyncRemoteDataSource {
    override suspend fun addToWatchlist(showId: TraktId) {
        val request = PostUsersListsListAddRequest(
            shows = listOf(
                PostUsersListsListAddRequestShowsInner(
                    ids = PostUsersListsListAddRequestShowsInnerOneOfIds(
                        trakt = showId.value,
                        slug = null,
                        imdb = null,
                        tmdb = null,
                        tvdb = 0,
                    ),
                    title = "",
                    year = 0,
                ),
            ),
        )
        syncApi.postSyncWatchlistAdd(request)
    }

    override suspend fun removeFromWatchlist(showId: TraktId) {
        val request = PostUsersListsListAddRequest(
            shows = listOf(
                PostUsersListsListAddRequestShowsInner(
                    ids = PostUsersListsListAddRequestShowsInnerOneOfIds(
                        trakt = showId.value,
                        slug = null,
                        imdb = null,
                        tmdb = null,
                        tvdb = 0,
                    ),
                    title = "",
                    year = 0,
                ),
            ),
        )
        syncApi.postSyncWatchlistRemove(request)
    }

    override suspend fun getWatchlist(
        sort: String,
        page: Int?,
        limit: Int?,
        extended: String?,
    ): List<WatchlistShowDto> {
        val response = usersApi.getUsersWatchlistShows(
            id = "me",
            sort = sort,
            extended = extended,
            page = page,
            limit = limit,
            watchnow = null,
            genres = null,
            years = null,
            ratings = null,
            startDate = null,
            endDate = null,
        )

        return response.body()
    }

    override suspend fun getUpNextProgress(
        limit: Int,
        page: Int,
    ): List<ProgressShowDto> {
        val response = syncApi.getSyncProgressUpNextNitro(
            page = page,
            limit = limit,
        )
        return response.body()
    }

    override suspend fun addToHistory(
        showId: TraktId,
        watchedAt: ZonedDateTime,
    ): SyncAddHistoryResponseDto {
        val request = PostUsersListsListAddRequest(
            shows = listOf(
                PostUsersListsListAddRequestShowsInner(
                    ids = PostUsersListsListAddRequestShowsInnerOneOfIds(
                        trakt = showId.value,
                        slug = null,
                        imdb = null,
                        tmdb = null,
                        tvdb = 0,
                    ),
                    title = "",
                    year = 0,
                    watchedAt = watchedAt.format(ISO_INSTANT),
                ),
            ),
        )

        val result = syncApi.postSyncHistoryAdd(request)
        return result.body()
    }

    override suspend fun getWatched(extended: String?): List<WatchedShowDto> {
        val response = watchedApi.getUsersWatchedShows(
            id = "me",
            extended = extended,
            hidden = null,
            specials = true,
            countSpecials = null,
        )
        return response.body()
    }

    /** Example (shows -> seasons -> episodes):
     * {
     *     "150469": {
     *         "462717": {
     *             "13101183": "2025-09-01T10:58:10.000Z"
     *         }
     *     },
     *     "249647": {
     *         "404720": {
     *             "12142723": "2025-09-01T10:57:43.000Z",
     *             "12853592": "2025-09-01T10:57:43.000Z",
     *         }
     *     }
     * }
     */
    override suspend fun getShowsPlexCollection(): Map<TraktId, Map<TraktId, TraktId>> {
        val response = collectionApi.getSyncCollectionMinimalShows(
            extended = "min",
            availableOn = "plex",
        )
        return response.body()
            .map {
                val showId = it.key.toInt().toTraktId()
                showId to it.value.map { entry ->
                    val seasonId = entry.key.toInt().toTraktId()
                    val episodeId = entry.value.toInt().toTraktId()
                    seasonId to episodeId
                }.toMap()
            }
            .toMap()
    }

    /** Example:
     * {
     *     "12142723": "2025-09-01T10:57:43.000Z",
     *     "12853592": "2025-09-01T10:57:43.000Z",
     * }
     */
    override suspend fun getEpisodesPlexCollection(): Map<TraktId, String> {
        val response = collectionApi.getSyncCollectionMinimalEpisodes(
            extended = "min",
            availableOn = "plex",
        )
        return response.body()
            .map { it.key.toInt().toTraktId() to it.value }
            .toMap()
    }
}
