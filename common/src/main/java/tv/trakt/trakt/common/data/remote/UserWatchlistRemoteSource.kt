package tv.trakt.trakt.common.data.remote

import org.openapitools.client.apis.SyncApi
import org.openapitools.client.apis.UsersApi
import org.openapitools.client.models.PostUsersListsReorderRequest
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.model.globalfilter.GlobalFilter
import tv.trakt.trakt.common.model.sorting.SortType
import tv.trakt.trakt.common.model.sorting.Sorting
import tv.trakt.trakt.common.networking.WatchlistItemDto
import tv.trakt.trakt.common.networking.WatchlistMovieDto
import tv.trakt.trakt.common.networking.WatchlistShowDto
import tv.trakt.trakt.common.networking.api.v3.V3Api
import tv.trakt.trakt.common.networking.helpers.CacheMarkerProvider

class UserWatchlistRemoteSource(
    private val usersApi: UsersApi,
    private val syncApi: SyncApi,
    private val v3Api: V3Api,
    private val cacheMarker: CacheMarkerProvider,
) {
    suspend fun getWatchlistMinimal(): Pair<Set<TraktId>, Set<TraktId>> {
        return v3Api.getWatchlistMinimal()
    }

    suspend fun getWatchlist(
        page: Int? = null,
        limit: Int? = null,
        extended: String? = null,
        sorting: Sorting? = null,
        hide: String? = null,
        filters: GlobalFilter?,
    ): List<WatchlistItemDto> {
        val response = usersApi.getUsersWatchlistAll(
            id = "me",
            sort = "",
            extended = extended,
            page = page,
            limit = limit,
            watchnow = filters?.availability?.joinToString(",") { it.slug },
            genres = filters?.genre?.joinToString(",") { it.slug },
            subgenres = filters?.subgenre?.joinToString(","),
            years = filters?.years?.let { "${it.first}-${it.second}" },
            ratings = filters?.rating?.let { "${it.first}-${it.second}" },
            startDate = null,
            endDate = null,
            hide = hide,
            sortBy = sorting?.type?.value ?: SortType.Rank.value,
            sortHow = sorting?.order?.value,
            runtimes = filters?.runtime?.let { "${it.first}-${it.second}" },
            countries = filters?.countries?.joinToString(",") ?: filters?.region?.slug,
            statuses = filters?.statuses?.joinToString(",") { it.slug },
            certifications = filters?.certification?.joinToString(",") { it.slug },
            ignoreWatched = filters?.hideWatched,
        )

        return response.body()
    }

    suspend fun reorderWatchlist(itemIds: List<Int>) {
        syncApi.postSyncWatchlistReorder(
            postUsersListsReorderRequest = PostUsersListsReorderRequest(
                rank = itemIds,
            ),
        )
        cacheMarker.invalidate()
    }

    suspend fun getWatchlistShows(
        page: Int? = null,
        limit: Int? = null,
        extended: String? = null,
        sorting: Sorting? = null,
        hide: String? = null,
        filters: GlobalFilter? = null,
    ): List<WatchlistShowDto> {
        val response = usersApi.getUsersWatchlistShows(
            id = "me",
            extended = extended,
            page = page,
            limit = limit,
            hide = hide,
            watchnow = filters?.availability?.joinToString(",") { it.slug },
            genres = filters?.genre?.joinToString(",") { it.slug },
            subgenres = filters?.subgenre?.joinToString(","),
            years = filters?.years?.let { "${it.first}-${it.second}" },
            ratings = filters?.rating?.let { "${it.first}-${it.second}" },
            startDate = null,
            endDate = null,
            sort = sorting?.type?.value ?: SortType.Rank.value,
            sortHow = sorting?.order?.value,
            sortBy = null,
            runtimes = filters?.runtime?.let { "${it.first}-${it.second}" },
            countries = filters?.countries?.joinToString(",") ?: filters?.region?.slug,
            statuses = filters?.statuses?.joinToString(",") { it.slug },
            certifications = filters?.certification?.joinToString(",") { it.slug },
            ignoreWatched = filters?.hideWatched,
        )

        return response.body()
    }

    suspend fun getWatchlistMovies(
        page: Int? = null,
        limit: Int? = null,
        extended: String? = null,
        sorting: Sorting? = null,
        hide: String? = null,
        filters: GlobalFilter? = null,
    ): List<WatchlistMovieDto> {
        val response = usersApi.getUsersWatchlistMovies(
            id = "me",
            extended = extended,
            page = page,
            limit = limit,
            hide = hide,
            watchnow = filters?.availability?.joinToString(",") { it.slug },
            genres = filters?.genre?.joinToString(",") { it.slug },
            subgenres = filters?.subgenre?.joinToString(","),
            years = filters?.years?.let { "${it.first}-${it.second}" },
            ratings = filters?.rating?.let { "${it.first}-${it.second}" },
            startDate = null,
            endDate = null,
            sort = sorting?.type?.value ?: SortType.Rank.value,
            sortHow = sorting?.order?.value,
            sortBy = null,
            runtimes = filters?.runtime?.let { "${it.first}-${it.second}" },
            countries = filters?.countries?.joinToString(",") ?: filters?.region?.slug,
            statuses = filters?.statuses?.joinToString(",") { it.slug },
            certifications = filters?.certification?.joinToString(",") { it.slug },
            ignoreWatched = filters?.hideWatched,
        )

        return response.body()
    }
}
