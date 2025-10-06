package tv.trakt.trakt.core.sync.data.remote.movies

import org.openapitools.client.apis.SyncApi
import org.openapitools.client.models.PostCheckinMovieRequestMovieIds
import org.openapitools.client.models.PostSyncHistoryRemoveRequest
import org.openapitools.client.models.PostUsersListsListAddRequest
import org.openapitools.client.models.PostUsersListsListAddRequestMoviesInner
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.networking.helpers.CacheMarkerProvider
import java.time.Instant

internal class MoviesSyncApiClient(
    private val syncApi: SyncApi,
    private val cacheMarker: CacheMarkerProvider,
) : MoviesSyncRemoteDataSource {
    override suspend fun addToWatched(
        movieId: TraktId,
        watchedAt: Instant,
    ) {
        val request = PostUsersListsListAddRequest(
            movies = listOf(
                PostUsersListsListAddRequestMoviesInner(
                    ids = PostCheckinMovieRequestMovieIds(
                        trakt = movieId.value,
                        slug = null,
                        imdb = null,
                        tmdb = 0,
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

    override suspend fun removeSingleFromHistory(playId: Long) {
        val request = PostSyncHistoryRemoveRequest(
            ids = listOf(playId),
        )
        syncApi.postSyncHistoryRemove(request)
        cacheMarker.invalidate()
    }

    override suspend fun removeAllFromHistory(movieId: TraktId) {
        val request = PostSyncHistoryRemoveRequest(
            movies = listOf(
                PostUsersListsListAddRequestMoviesInner(
                    ids = PostCheckinMovieRequestMovieIds(
                        trakt = movieId.value,
                        slug = null,
                        imdb = null,
                        tmdb = 0,
                    ),
                    title = "",
                    year = 0,
                ),
            ),
        )
        syncApi.postSyncHistoryRemove(request)
        cacheMarker.invalidate()
    }

    override suspend fun addToWatchlist(movieId: TraktId) {
        val request = PostUsersListsListAddRequest(
            movies = listOf(
                PostUsersListsListAddRequestMoviesInner(
                    ids = PostCheckinMovieRequestMovieIds(
                        trakt = movieId.value,
                        slug = null,
                        imdb = null,
                        tmdb = 0,
                    ),
                    title = "",
                    year = 0,
                ),
            ),
        )
        syncApi.postSyncWatchlistAdd(request)
        cacheMarker.invalidate()
    }

    override suspend fun removeFromWatchlist(movieId: TraktId) {
        val request = PostUsersListsListAddRequest(
            movies = listOf(
                PostUsersListsListAddRequestMoviesInner(
                    ids = PostCheckinMovieRequestMovieIds(
                        trakt = movieId.value,
                        slug = null,
                        imdb = null,
                        tmdb = 0,
                    ),
                    title = "",
                    year = 0,
                ),
            ),
        )
        syncApi.postSyncWatchlistRemove(request)
        cacheMarker.invalidate()
    }
}
