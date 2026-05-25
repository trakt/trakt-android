package tv.trakt.trakt.common.core.user.data.remote.watchlist

import org.openapitools.client.apis.UsersApi
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.model.globalfilter.GlobalFilter
import tv.trakt.trakt.common.model.sorting.SortType
import tv.trakt.trakt.common.model.sorting.Sorting
import tv.trakt.trakt.common.networking.WatchlistItemDto
import tv.trakt.trakt.common.networking.WatchlistMovieDto
import tv.trakt.trakt.common.networking.WatchlistShowDto
import tv.trakt.trakt.common.networking.api.v3.V3Api

class UserWatchlistApiClient(
    private val usersApi: UsersApi,
    private val v3Api: V3Api,
) : UserWatchlistRemoteDataSource {
    override suspend fun getWatchlistMinimal(): Pair<Set<TraktId>, Set<TraktId>> {
        return v3Api.getWatchlistMinimal()
    }

    override suspend fun getWatchlist(
        page: Int?,
        limit: Int?,
        extended: String?,
        sorting: Sorting?,
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
            hide = null,
            sortBy = sorting?.type?.value ?: SortType.RANK.value,
            sortHow = sorting?.order?.value,
            runtimes = filters?.runtime?.let { "${it.first}-${it.second}" },
            countries = filters?.countries?.joinToString(",") ?: filters?.region?.slug,
            certifications = filters?.certification?.joinToString(",") { it.slug },
        )

        return response.body()
    }

    override suspend fun getWatchlistShows(
        page: Int?,
        limit: Int?,
        extended: String?,
        sorting: Sorting?,
        hide: String?,
        filters: GlobalFilter?,
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
            sort = sorting?.type?.value ?: SortType.RANK.value,
            sortHow = sorting?.order?.value,
            sortBy = null,
            runtimes = filters?.runtime?.let { "${it.first}-${it.second}" },
            countries = filters?.countries?.joinToString(",") ?: filters?.region?.slug,
            certifications = filters?.certification?.joinToString(",") { it.slug },
        )

        return response.body()
    }

    override suspend fun getWatchlistMovies(
        page: Int?,
        limit: Int?,
        extended: String?,
        sorting: Sorting?,
        hide: String?,
        filters: GlobalFilter?,
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
            sort = sorting?.type?.value ?: SortType.RANK.value,
            sortHow = sorting?.order?.value,
            sortBy = null,
            runtimes = filters?.runtime?.let { "${it.first}-${it.second}" },
            countries = filters?.countries?.joinToString(",") ?: filters?.region?.slug,
            certifications = filters?.certification?.joinToString(",") { it.slug },
        )

        return response.body()
    }
}
