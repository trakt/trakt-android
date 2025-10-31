package tv.trakt.trakt.core.sync.data.remote.shows

import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import org.openapitools.client.apis.SyncApi
import org.openapitools.client.apis.UsersApi
import org.openapitools.client.models.PostSyncHistoryRemoveRequest
import org.openapitools.client.models.PostUsersListsListAddRequest
import org.openapitools.client.models.PostUsersListsListAddRequestShowsInner
import org.openapitools.client.models.PostUsersListsListAddRequestShowsInnerOneOfIds
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.networking.ProgressShowDto
import tv.trakt.trakt.common.networking.helpers.CacheMarkerProvider
import java.time.Instant

internal class ShowsSyncApiClient(
    private val syncApi: SyncApi,
    private val usersApi: UsersApi,
    private val cacheMarker: CacheMarkerProvider,
) : ShowsSyncRemoteDataSource {
    override suspend fun getUpNext(
        limit: Int,
        page: Int,
    ): List<ProgressShowDto> {
        val response = syncApi.getSyncProgressUpNextNitro(
            page = page,
            limit = limit,
            intent = "continue",
        )
        return response.body()
    }

    override suspend fun addToWatched(
        showId: TraktId,
        watchedAt: Instant,
    ) {
        val request = PostUsersListsListAddRequest(
            shows = listOf(
                PostUsersListsListAddRequestShowsInner(
                    ids = PostUsersListsListAddRequestShowsInnerOneOfIds(
                        trakt = showId.value,
                        slug = null,
                        imdb = null,
                        tmdb = 0,
                        tvdb = 0,
                    ),
                    title = "",
                    year = 0,
                    watchedAt = watchedAt.toString(),
                ),
            ),
        )
        syncApi.postSyncHistoryAdd(request)
        cacheMarker.invalidate()
    }

    override suspend fun removeAllFromHistory(showId: TraktId) {
        val request = PostSyncHistoryRemoveRequest(
            shows = listOf(
                PostUsersListsListAddRequestShowsInner(
                    ids = PostUsersListsListAddRequestShowsInnerOneOfIds(
                        trakt = showId.value,
                        slug = null,
                        imdb = null,
                        tmdb = 0,
                        tvdb = 0,
                    ),
                    title = "",
                    year = 0,
                ),
            ),
        )
        syncApi.postSyncHistoryRemove(request)
        cacheMarker.invalidate()
    }

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
        cacheMarker.invalidate()
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
        cacheMarker.invalidate()
    }

    override suspend fun dropShow(showId: TraktId) {
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

        coroutineScope {
            val droppedAsync = async {
                usersApi.postUsersHiddenAdd(
                    section = "dropped",
                    postUsersListsListAddRequest = request,
                )
            }
            val calendarAsync = async {
                usersApi.postUsersHiddenAdd(
                    section = "calendar",
                    postUsersListsListAddRequest = request,
                )
            }
            droppedAsync.await()
            calendarAsync.await()
        }

        cacheMarker.invalidate()
    }
}
