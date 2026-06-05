package tv.trakt.trakt.app.core.sync.data.remote.shows

import org.openapitools.client.apis.CollectionApi
import org.openapitools.client.apis.SyncApi
import org.openapitools.client.apis.UsersApi
import org.openapitools.client.models.PostCheckinStartRequestOneOfOneOfEpisodeIds
import org.openapitools.client.models.PostSyncHistoryRemoveRequest
import org.openapitools.client.models.PostUsersListsListAddRequest
import org.openapitools.client.models.PostUsersListsListAddRequestShowsInner
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.model.toTraktId
import tv.trakt.trakt.common.networking.ProgressShowDto
import tv.trakt.trakt.common.networking.SyncAddHistoryResponseDto
import tv.trakt.trakt.common.networking.WatchlistShowDto
import tv.trakt.trakt.common.networking.helpers.CacheMarkerProvider

internal class ShowsSyncApiClient(
    private val usersApi: UsersApi,
    private val syncApi: SyncApi,
    private val collectionApi: CollectionApi,
    private val cacheMarkerProvider: CacheMarkerProvider,
) : ShowsSyncRemoteDataSource {
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
        cacheMarkerProvider.invalidate()
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
        cacheMarkerProvider.invalidate()
    }

    override suspend fun getWatchlist(
        sort: String,
        page: Int?,
        limit: Int?,
        extended: String?,
        hide: String?,
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
            subgenres = null,
            hide = hide,
            sortBy = null,
            sortHow = null,
            runtimes = null,
            countries = null,
            certifications = null,
        )

        return response.body()
    }

    override suspend fun getUpNextProgress(
        limit: Int,
        page: Int,
        intent: String,
        sortBy: String?,
        sortHow: String?,
    ): List<ProgressShowDto> {
        val response = syncApi.getSyncProgressUpNextNitro(
            page = page,
            limit = limit,
            sortBy = sortBy,
            sortHow = sortHow,
            intent = intent,
            watchnow = null,
            genres = null,
            subgenres = null,
            years = null,
            ratings = null,
            startDate = null,
            endDate = null,
            runtimes = null,
            countries = null,
            certifications = null,
        )
        return response.body()
    }

    override suspend fun addToHistory(
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
                        tmdb = null,
                        tvdb = 0,
                    ),
                    title = "",
                    year = 0,
                    watchedAt = watchedAt,
                ),
            ),
        )

        val result = syncApi.postSyncHistoryAdd(request)
        cacheMarkerProvider.invalidate()
        return result.body()
    }

    override suspend fun removeFromHistory(showId: TraktId) {
        val request = PostSyncHistoryRemoveRequest(
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
        syncApi.postSyncHistoryRemove(request)
        cacheMarkerProvider.invalidate()
    }

    override suspend fun getWatched(): Map<String, Map<String, Map<String, List<String>>>> {
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

    override suspend fun getShowsPlexCollection(): Map<TraktId, Map<TraktId, Map<TraktId, String>>> {
        val response = collectionApi.getSyncCollectionMinimalShows(
            availableOn = "plex",
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

    override suspend fun getEpisodesPlexCollection(): Map<TraktId, String> {
        val response = collectionApi.getSyncCollectionMinimalEpisodes(
            availableOn = "plex",
        )
        return response.body()
            .map { it.key.toInt().toTraktId() to it.value }
            .toMap()
    }
}
