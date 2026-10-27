package tv.trakt.trakt.common.data.remote

import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import org.openapitools.client.apis.CollectionApi
import org.openapitools.client.apis.RecommendationsApi
import org.openapitools.client.apis.SyncApi
import org.openapitools.client.apis.UsersApi
import org.openapitools.client.models.PostCheckinStartRequestOneOfOneOfEpisodeIds
import org.openapitools.client.models.PostSyncFavoritesAddRequest
import org.openapitools.client.models.PostSyncHistoryRemoveRequest
import org.openapitools.client.models.PostSyncRatingsRemoveRequestShowsInner
import org.openapitools.client.models.PostUsersListsListAddRequest
import org.openapitools.client.models.PostUsersListsListAddRequestShowsInner
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.model.globalfilter.GlobalFilter
import tv.trakt.trakt.common.model.lists.ParentalGuideCategory
import tv.trakt.trakt.common.model.sorting.Sorting
import tv.trakt.trakt.common.model.toTraktId
import tv.trakt.trakt.common.networking.ProgressShowDto
import tv.trakt.trakt.common.networking.SyncAddHistoryResponseDto
import tv.trakt.trakt.common.networking.WatchlistShowDto
import tv.trakt.trakt.common.networking.helpers.CacheMarkerProvider

class ShowsSyncRemoteSource(
    private val syncApi: SyncApi,
    private val usersApi: UsersApi,
    private val recommendationsApi: RecommendationsApi,
    private val cacheMarker: CacheMarkerProvider,
    private val collectionApi: CollectionApi,
) {
    suspend fun getUpNext(
        limit: Int,
        page: Int,
        intent: String,
        sortBy: String?,
        sortHow: String?,
        filters: GlobalFilter? = null,
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
            statuses = filters?.statuses?.joinToString(",") { it.slug },
            certifications = filters?.certification?.joinToString(",") { it.slug },
            parentalNudity = filters?.parentalParam(ParentalGuideCategory.Nudity),
            parentalViolence = filters?.parentalParam(ParentalGuideCategory.Violence),
            parentalProfanity = filters?.parentalParam(ParentalGuideCategory.Profanity),
            parentalAlcohol = filters?.parentalParam(ParentalGuideCategory.Alcohol),
            parentalFrightening = filters?.parentalParam(ParentalGuideCategory.Frightening),
        )
        return response.body()
    }

    suspend fun addToWatched(
        showId: TraktId,
        watchedAt: String,
    ): SyncAddHistoryResponseDto {
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
        val response = syncApi.postSyncHistoryAdd(request)
        cacheMarker.invalidate()
        return response.body()
    }

    suspend fun removeAllFromHistory(showId: TraktId) {
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

    suspend fun addToWatchlist(showId: TraktId) {
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

    suspend fun removeFromWatchlist(showId: TraktId) {
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

    suspend fun dropShow(showId: TraktId) {
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

    suspend fun hideRecommendation(showId: TraktId) {
        recommendationsApi.deleteRecommendationsShowsHide(id = showId.value.toString())
        cacheMarker.invalidate()
    }

    suspend fun addToFavorites(showId: TraktId) {
        val request = PostSyncFavoritesAddRequest(
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

    suspend fun removeFromFavorites(showId: TraktId) {
        val request = PostSyncFavoritesAddRequest(
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

    suspend fun getWatchlist(
        sorting: Sorting = Sorting.Default,
        page: Int? = null,
        limit: Int? = null,
        extended: String? = null,
        hide: String? = null,
    ): List<WatchlistShowDto> {
        val response = usersApi.getUsersWatchlistShows(
            id = "me",
            sort = "",
            extended = extended,
            page = page,
            limit = limit,
            watchnow = null,
            genres = null,
            years = null,
            ratings = null,
            startDate = null,
            endDate = null,
            subgenres = null,
            hide = hide,
            sortBy = sorting.type.value,
            sortHow = sorting.order.value,
            runtimes = null,
            countries = null,
            certifications = null,
        )

        return response.body()
    }

    suspend fun getWatched(): Map<String, Map<String, Map<String, List<String>>>> {
        val result = mutableMapOf<String, Map<String, Map<String, List<String>>>>()
        var page = 1

        while (true) {
            val pageResponse = usersApi.getUsersWatchedMinimalShows(
                id = "me",
                extended = "min",
                specials = true,
                seasonNumbers = true,
                page = page,
                limit = 100,
            ).body()

            if (pageResponse.isEmpty()) {
                break
            }

            result.putAll(pageResponse)
            page++
        }

        return result
    }

    suspend fun getShowsPlexCollection(): Map<TraktId, Map<TraktId, Map<TraktId, String>>> {
        val response = collectionApi.getSyncCollectionMinimalShows(
            availableOn = "digital",
        )

        return response.body()
            .map {
                val showId = it.key.toInt().toTraktId()
                showId to it.value.map { season ->
                    val seasonId = season.key.toInt().toTraktId()
                    val episodes = season.value.map { episode ->
                        val episodeId = episode.key.toInt().toTraktId()
                        episodeId to episode.value
                    }.toMap()
                    seasonId to episodes
                }.toMap()
            }
            .toMap()
    }

    suspend fun getEpisodesPlexCollection(): Map<TraktId, String> {
        val response = collectionApi.getSyncCollectionMinimalEpisodes(
            availableOn = "digital",
        )
        return response.body()
            .map { it.key.toInt().toTraktId() to it.value }
            .toMap()
    }
}
