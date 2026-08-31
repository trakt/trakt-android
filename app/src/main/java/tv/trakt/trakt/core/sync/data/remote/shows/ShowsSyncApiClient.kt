package tv.trakt.trakt.core.sync.data.remote.shows

import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import org.openapitools.client.apis.RecommendationsApi
import org.openapitools.client.apis.SyncApi
import org.openapitools.client.apis.UsersApi
import org.openapitools.client.models.PostCheckinStartRequestOneOfOneOfEpisodeIds
import org.openapitools.client.models.PostSyncHistoryRemoveRequest
import org.openapitools.client.models.PostSyncRatingsRemoveRequestShowsInner
import org.openapitools.client.models.PostUsersListsListAddRequest
import org.openapitools.client.models.PostUsersListsListAddRequestShowsInner
import org.openapitools.client.models.PutSyncFavoritesUpdateRequest
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.model.globalfilter.GlobalFilter
import tv.trakt.trakt.common.networking.ProgressShowDto
import tv.trakt.trakt.common.networking.helpers.CacheMarkerProvider

internal class ShowsSyncApiClient(
    private val syncApi: SyncApi,
    private val usersApi: UsersApi,
    private val recommendationsApi: RecommendationsApi,
    private val cacheMarker: CacheMarkerProvider,
) : ShowsSyncRemoteDataSource {
    override suspend fun getUpNext(
        limit: Int,
        page: Int,
        intent: String,
        sortBy: String?,
        sortHow: String?,
        filters: GlobalFilter?,
    ): List<ProgressShowDto> {
        val response = syncApi.getSyncProgressUpNextNitro(
            page = page,
            limit = limit,
            intent = intent,
            sortBy = sortBy,
            sortHow = sortHow,
            watchnow = filters?.availability?.joinToString(",") { it.slug },
            genres = filters?.genre?.joinToString(",") { it.slug },
            subgenres = filters?.subgenre?.joinToString(","),
            years = filters?.years?.let { "${it.first}-${it.second}" },
            ratings = filters?.rating?.let { "${it.first}-${it.second}" },
            startDate = null,
            endDate = null,
            runtimes = filters?.runtime?.let { "${it.first}-${it.second}" },
            countries = filters?.countries?.joinToString(",") ?: filters?.region?.slug,
            certifications = filters?.certification?.joinToString(",") { it.slug },
        )
        return response.body()
    }

    override suspend fun addToWatched(
        showId: TraktId,
        watchedAt: String,
    ) {
        val request = PostUsersListsListAddRequest(
            shows = listOf(
                PostUsersListsListAddRequestShowsInner(
                    ids = PostCheckinStartRequestOneOfOneOfEpisodeIds(
                        trakt = showId.value,
                        slug = null,
                        imdb = null,
                        tmdb = 0,
                        tvdb = 0,
                    ),
                    title = "",
                    year = 0,
                    watchedAt = watchedAt,
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
                    ids = PostCheckinStartRequestOneOfOneOfEpisodeIds(
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
                    ids = PostCheckinStartRequestOneOfOneOfEpisodeIds(
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
                    ids = PostCheckinStartRequestOneOfOneOfEpisodeIds(
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
                    ids = PostCheckinStartRequestOneOfOneOfEpisodeIds(
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

    override suspend fun hideRecommendation(showId: TraktId) {
        recommendationsApi.deleteRecommendationsShowsHide(id = showId.value)
        cacheMarker.invalidate()
    }

    override suspend fun addToFavorites(showId: TraktId) {
        val request = PutSyncFavoritesUpdateRequest(
            shows = listOf(
                PostSyncRatingsRemoveRequestShowsInner(
                    ids = PostCheckinStartRequestOneOfOneOfEpisodeIds(
                        trakt = showId.value,
                        slug = null,
                        imdb = null,
                        tvdb = -1,
                        tmdb = 0,
                    ),
                ),
            ),
        )
        syncApi.postSyncFavoritesAdd(request)
        cacheMarker.invalidate()
    }

    override suspend fun removeFromFavorites(showId: TraktId) {
        val request = PutSyncFavoritesUpdateRequest(
            shows = listOf(
                PostSyncRatingsRemoveRequestShowsInner(
                    ids = PostCheckinStartRequestOneOfOneOfEpisodeIds(
                        trakt = showId.value,
                        slug = null,
                        imdb = null,
                        tvdb = -1,
                        tmdb = 0,
                    ),
                ),
            ),
        )
        syncApi.postSyncFavoritesRemove(request)
        cacheMarker.invalidate()
    }
}
