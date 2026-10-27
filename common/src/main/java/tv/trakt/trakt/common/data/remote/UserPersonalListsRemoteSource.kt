package tv.trakt.trakt.common.data.remote

import org.openapitools.client.apis.UsersApi
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.model.globalfilter.GlobalFilter
import tv.trakt.trakt.common.model.lists.ParentalGuideCategory
import tv.trakt.trakt.common.model.pagination.Pagination
import tv.trakt.trakt.common.model.sorting.ListsSorting
import tv.trakt.trakt.common.model.sorting.Sorting
import tv.trakt.trakt.common.model.toTraktId
import tv.trakt.trakt.common.networking.ListDto
import tv.trakt.trakt.common.networking.ListItemDto
import tv.trakt.trakt.common.networking.ListMovieItemDto
import tv.trakt.trakt.common.networking.ListShowItemDto
import tv.trakt.trakt.common.networking.api.v3.V3Api
import tv.trakt.trakt.common.networking.api.v3.model.V3MinimalList

class UserPersonalListsRemoteSource(
    private val usersApi: UsersApi,
    private val v3Api: V3Api,
) {
    suspend fun getPersonalLists(
        pagination: Pagination,
        userId: String = "me",
        sorting: ListsSorting? = null,
    ): List<ListDto> {
        val response = usersApi.getUsersListsPersonal(
            id = userId,
            extended = "images",
            sortBy = sorting?.type?.value,
            sortHow = sorting?.order?.value,
            page = pagination.page,
            limit = pagination.limit,
        )
        return response.body()
    }

    suspend fun getPersonalListsMinimal(): List<V3MinimalList> {
        return v3Api.getListsMinimal()
    }

    suspend fun getPersonalListItems(
        listId: TraktId,
        limit: Int,
        page: Int = 1,
        extended: String,
        sorting: Sorting,
        filters: GlobalFilter? = null,
    ): List<ListItemDto> {
        val response = usersApi.getUsersListsListItemsAll(
            id = "me",
            listId = listId.value.toString(),
            extended = extended,
            sortBy = sorting.type.value,
            sortHow = sorting.order.value,
            watchnow = filters?.availability?.joinToString(",") { it.slug },
            genres = filters?.genre?.joinToString(",") { it.slug },
            years = filters?.years?.let { "${it.first}-${it.second}" },
            subgenres = filters?.subgenre?.joinToString(","),
            ratings = filters?.rating?.let { "${it.first}-${it.second}" },
            startDate = null,
            endDate = null,
            runtimes = filters?.runtime?.let { "${it.first}-${it.second}" },
            page = page,
            limit = limit.toString(),
            countries = filters?.countries?.joinToString(",") ?: filters?.region?.slug,
            statuses = filters?.statuses?.joinToString(",") { it.slug },
            certifications = filters?.certification?.joinToString(",") { it.slug },
            parentalNudity = filters?.parentalParam(ParentalGuideCategory.Nudity),
            parentalViolence = filters?.parentalParam(ParentalGuideCategory.Violence),
            parentalProfanity = filters?.parentalParam(ParentalGuideCategory.Profanity),
            parentalAlcohol = filters?.parentalParam(ParentalGuideCategory.Alcohol),
            parentalFrightening = filters?.parentalParam(ParentalGuideCategory.Frightening),
            ignoreWatched = filters?.hideWatched,
            ignoreWatchlisted = filters?.hideWatchlist,
            ignoreCollected = null,
        )

        return response.body()
    }

    suspend fun getPersonalListShowItems(
        listId: TraktId,
        limit: Int?,
        page: Int = 1,
        extended: String,
        sorting: Sorting,
        filters: GlobalFilter? = null,
    ): List<ListShowItemDto> {
        val response = usersApi.getUsersListsListItemsShow(
            id = "me",
            listId = listId.value.toString(),
            extended = extended,
            sortBy = sorting.type.value,
            sortHow = sorting.order.value,
            watchnow = filters?.availability?.joinToString(",") { it.slug },
            genres = filters?.genre?.joinToString(",") { it.slug },
            years = filters?.years?.let { "${it.first}-${it.second}" },
            subgenres = filters?.subgenre?.joinToString(","),
            ratings = filters?.rating?.let { "${it.first}-${it.second}" },
            startDate = null,
            endDate = null,
            runtimes = filters?.runtime?.let { "${it.first}-${it.second}" },
            page = page,
            limit = when {
                limit == null -> "all"
                else -> limit.toString()
            },
            countries = filters?.countries?.joinToString(",") ?: filters?.region?.slug,
            statuses = filters?.statuses?.joinToString(",") { it.slug },
            certifications = filters?.certification?.joinToString(",") { it.slug },
            parentalNudity = filters?.parentalParam(ParentalGuideCategory.Nudity),
            parentalViolence = filters?.parentalParam(ParentalGuideCategory.Violence),
            parentalProfanity = filters?.parentalParam(ParentalGuideCategory.Profanity),
            parentalAlcohol = filters?.parentalParam(ParentalGuideCategory.Alcohol),
            parentalFrightening = filters?.parentalParam(ParentalGuideCategory.Frightening),
            ignoreWatched = filters?.hideWatched,
            ignoreWatchlisted = filters?.hideWatchlist,
            ignoreCollected = null,
        )

        return response.body()
    }

    suspend fun getPersonalListMovieItems(
        listId: TraktId,
        limit: Int?,
        page: Int = 1,
        extended: String,
        sorting: Sorting,
        filters: GlobalFilter? = null,
    ): List<ListMovieItemDto> {
        val response = usersApi.getUsersListsListItemsMovie(
            id = "me",
            listId = listId.value.toString(),
            extended = extended,
            sortBy = sorting.type.value,
            sortHow = sorting.order.value,
            watchnow = filters?.availability?.joinToString(",") { it.slug },
            genres = filters?.genre?.joinToString(",") { it.slug },
            years = filters?.years?.let { "${it.first}-${it.second}" },
            subgenres = filters?.subgenre?.joinToString(","),
            ratings = filters?.rating?.let { "${it.first}-${it.second}" },
            startDate = null,
            endDate = null,
            runtimes = filters?.runtime?.let { "${it.first}-${it.second}" },
            page = page,
            limit = when {
                limit == null -> "all"
                else -> limit.toString()
            },
            countries = filters?.countries?.joinToString(",") ?: filters?.region?.slug,
            statuses = filters?.statuses?.joinToString(",") { it.slug },
            certifications = filters?.certification?.joinToString(",") { it.slug },
            parentalNudity = filters?.parentalParam(ParentalGuideCategory.Nudity),
            parentalViolence = filters?.parentalParam(ParentalGuideCategory.Violence),
            parentalProfanity = filters?.parentalParam(ParentalGuideCategory.Profanity),
            parentalAlcohol = filters?.parentalParam(ParentalGuideCategory.Alcohol),
            parentalFrightening = filters?.parentalParam(ParentalGuideCategory.Frightening),
            ignoreWatched = filters?.hideWatched,
            ignoreWatchlisted = filters?.hideWatchlist,
            ignoreCollected = null,
        )

        return response.body()
    }

    suspend fun getShowLists(showId: TraktId): Set<TraktId> {
        val response = v3Api.getShowMeLists(showId)
        return response.map { it.toTraktId() }.toSet()
    }

    suspend fun getMovieLists(movieId: TraktId): Set<TraktId> {
        val response = v3Api.getMovieMeLists(movieId)
        return response.map { it.toTraktId() }.toSet()
    }
}
