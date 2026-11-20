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
import tv.trakt.trakt.common.networking.UpdateSmartListRequestDto
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

    suspend fun updateSmartList(
        listId: TraktId,
        request: UpdateSmartListRequestDto,
        userId: String = "me",
    ) {
        val response = smartListsApi.putUsersSmartListsSmartListUpdate(
            id = userId,
            listId = listId.value.toString(),
            putUsersSmartListsSmartListUpdateRequest = request,
        )
        check(response.success) { "Smart list update failed with status ${response.status}" }
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
        extended: String = "full,colors",
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
        extended: String = "full,colors",
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

private fun SmartListFilters.toPreviewParams(): Map<String, String> = withoutShowOnlyFilters().toTypedPreviewParams()

private fun SmartListFilters.toTypedPreviewParams(): Map<String, String> {
    val mediaType = when (media) {
        MediaMode.Media -> "media"
        MediaMode.Shows -> "shows"
        MediaMode.Movies -> "movies"
    }

    return listOf(
        "source" to source.value,
        "media_type" to mediaType,
        "genres" to genreTokens.joinToList(),
        "genres_operator" to "and".takeIf { genresMatchAll },
        "subgenres" to subgenres?.joinToList(),
        "certifications" to certificationTokens.joinToList(),
        "languages" to languages?.joinToList(),
        "countries" to countryTokens.joinToList(),
        "statuses" to statuses?.joinToList(),
        "networks" to networks?.joinToList(),
        "watchnow" to availability?.joinToList { it.slug },
        "watchnow_country" to watchnowCountry,
        "awards" to awards?.joinToList(),
        "award_years" to awardYears?.joinToRange(),
        "award_won" to awardWon.toFlagParam(),
        "years" to years?.joinToRange(),
        "ratings" to ratings?.joinToRange(),
        "runtimes" to runtimes?.joinToRange(),
        "imdb_ratings" to imdbRatings?.joinToRange(),
        "rt_meters" to rtMeters?.joinToRange(),
        "rt_user_meters" to rtUserMeters?.joinToRange(),
        "tmdb_ratings" to tmdbRatings?.joinToRange(),
        "letterboxd_ratings" to letterboxdRatings?.joinToRange(),
        "mal_ratings" to malRatings?.joinToRange(),
        "votes" to votes?.joinToRange(),
        "imdb_votes" to imdbVotes?.joinToRange(),
        "released_within_days" to releasedWithinDays?.toString(),
        "parental_include_unrated" to parentalIncludeUnrated.toFlagParam(),
        "ignore_watched" to ignoreWatched.toFlagParam(),
        "ignore_watchlisted" to ignoreWatchlisted.toFlagParam(),
        "ignore_watching" to ignoreWatching.toFlagParam(),
        "ignore_unreleased" to ignoreUnreleased.toFlagParam(),
        "ignore_released" to ignoreReleased.toFlagParam(),
        "ignore_ended" to ignoreEnded.toFlagParam(),
        "ignore_airing" to ignoreAiring.toFlagParam(),
        "ignore_no_release_date" to ignoreNoReleaseDate.toFlagParam(),
    ).plus(
        ParentalGuideCategory.entries.map { it.apiKey to parentalRange(it)?.joinToRange() },
    ).mapNotNull { (key, value) -> value?.let { key to it } }
        .toMap()
}

private fun <T> List<T>.joinToList(transform: (T) -> String = { it.toString() }): String? =
    takeIf { it.isNotEmpty() }?.joinToString(",", transform = transform)

private fun Boolean.toFlagParam(): String? = takeIf { it }?.toString()

private fun <T : Number> List<T>.joinToRange(): String? = takeIf { it.isNotEmpty() }?.joinToString("-")
