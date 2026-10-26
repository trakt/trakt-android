package tv.trakt.trakt.common.data.remote

import org.openapitools.client.apis.SmartListsApi
import tv.trakt.trakt.common.model.MediaMode
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.model.lists.ParentalGuideCategory
import tv.trakt.trakt.common.model.lists.SmartListFilters
import tv.trakt.trakt.common.model.pagination.Pagination
import tv.trakt.trakt.common.networking.CreateSmartListRequestDto
import tv.trakt.trakt.common.networking.SmartListDto
import tv.trakt.trakt.common.networking.SmartListItemDto
import tv.trakt.trakt.common.networking.api.v3.V3Api

class UserSmartListsRemoteSource(
    private val smartListsApi: SmartListsApi,
    private val v3Api: V3Api,
) {
    suspend fun getSmartLists(userId: String = "me"): List<SmartListDto> {
        return smartListsApi.getUsersSmartListsPersonal(id = userId).body()
    }

    suspend fun createSmartList(
        request: CreateSmartListRequestDto,
        userId: String = "me",
    ) {
        smartListsApi.postUsersSmartListsCreate(
            id = userId,
            postUsersListsListConvertToSmartListRequest = request,
        )
    }

    suspend fun getSmartListDetails(
        listId: TraktId,
        userId: String = "me",
    ): SmartListDto {
        return smartListsApi.getUsersSmartListsSmartListSummary(
            id = userId,
            listId = listId.value.toString(),
        ).body()
    }

    suspend fun getSmartListItems(
        listId: TraktId,
        pagination: Pagination = Pagination(),
        extended: String = "full,images,colors",
    ): List<SmartListItemDto> {
        return smartListsApi.getSmartListsItems(
            listId = listId.value.toString(),
            extended = extended,
            watchnow = null,
            genres = null,
            subgenres = null,
            years = null,
            ratings = null,
            runtimes = null,
            countries = null,
            certifications = null,
            ignoreWatched = null,
            ignoreWatchlisted = null,
            page = pagination.page,
            limit = pagination.limit.toString(),
        ).body()
    }

    suspend fun getSmartListPreviewItems(
        filters: SmartListFilters,
        pagination: Pagination = Pagination(),
        extended: String = "full,images,colors",
    ): List<SmartListItemDto> {
        return v3Api.getSmartListPreviewItems(
            params = filters.toPreviewParams() + ("extended" to extended),
            pagination = pagination,
        )
    }

    suspend fun deleteSmartList(
        listId: TraktId,
        userId: String = "me",
    ) {
        smartListsApi.deleteUsersSmartListsSmartListDelete(
            id = userId,
            listId = listId.value.toString(),
        )
    }
}

private fun SmartListFilters.toPreviewParams(): Map<String, String> {
    val mediaType = when (media) {
        MediaMode.Media -> "media"
        MediaMode.Shows -> "shows"
        MediaMode.Movies -> "movies"
    }

    return listOf(
        "source" to source.value,
        "media_type" to mediaType,
        "genres" to genres?.joinToList { it.slug },
        "subgenres" to subgenres?.joinToList(),
        "certifications" to certifications?.joinToList(),
        "languages" to languages?.joinToList(),
        "countries" to countries?.joinToList(),
        "statuses" to statuses?.joinToList(),
        "networks" to networks?.joinToList(),
        "watchnow" to availability?.joinToList { it.slug },
        "years" to years?.joinToRange(),
        "ratings" to ratings?.joinToRange(),
        "runtimes" to runtimes?.joinToRange(),
        "imdb_ratings" to imdbRatings?.joinToRange(),
        "rt_meters" to rtMeters?.joinToRange(),
        "rt_user_meters" to rtUserMeters?.joinToRange(),
        "ignore_watched" to ignoreWatched.takeIf { it }?.toString(),
        "ignore_watchlisted" to ignoreWatchlisted.takeIf { it }?.toString(),
    ).plus(
        ParentalGuideCategory.entries.map { it.apiKey to parentalRange(it)?.joinToRange() },
    ).mapNotNull { (key, value) -> value?.let { key to it } }
        .toMap()
}

private fun <T> List<T>.joinToList(transform: (T) -> String = { it.toString() }): String? =
    takeIf { it.isNotEmpty() }?.joinToString(",", transform = transform)

private fun <T : Number> List<T>.joinToRange(): String? = takeIf { it.isNotEmpty() }?.joinToString("-")
