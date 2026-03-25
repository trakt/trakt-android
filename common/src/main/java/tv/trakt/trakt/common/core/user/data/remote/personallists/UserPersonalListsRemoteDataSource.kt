package tv.trakt.trakt.common.core.user.data.remote.personallists

import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.model.pagination.Pagination
import tv.trakt.trakt.common.model.sorting.Sorting
import tv.trakt.trakt.common.networking.ListDto
import tv.trakt.trakt.common.networking.ListItemDto
import tv.trakt.trakt.common.networking.ListMovieItemDto
import tv.trakt.trakt.common.networking.ListShowItemDto
import tv.trakt.trakt.common.networking.api.v3.model.V3MinimalList

interface UserPersonalListsRemoteDataSource {
    suspend fun getPersonalLists(pagination: Pagination): List<ListDto>

    suspend fun getPersonalListsMinimal(): List<V3MinimalList>

    suspend fun getPersonalListItems(
        listId: TraktId,
        limit: Int,
        page: Int = 1,
        extended: String,
        sorting: Sorting,
    ): List<ListItemDto>

    suspend fun getPersonalListShowItems(
        listId: TraktId,
        limit: Int?,
        page: Int = 1,
        extended: String,
        sorting: Sorting,
    ): List<ListShowItemDto>

    suspend fun getPersonalListMovieItems(
        listId: TraktId,
        limit: Int?,
        page: Int = 1,
        extended: String,
        sorting: Sorting,
    ): List<ListMovieItemDto>

    suspend fun getMovieLists(movieId: TraktId): Set<TraktId>

    suspend fun getShowLists(showId: TraktId): Set<TraktId>
}
