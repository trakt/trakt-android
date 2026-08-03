package tv.trakt.trakt.core.lists.features.smart.usecase

import org.openapitools.client.models.PostUsersSmartListsCreateRequest
import tv.trakt.trakt.common.core.user.data.remote.smartlists.UserSmartListsRemoteDataSource
import tv.trakt.trakt.common.model.MediaMode
import tv.trakt.trakt.common.model.MediaMode.Movies
import tv.trakt.trakt.common.model.MediaMode.Shows
import tv.trakt.trakt.common.model.lists.SmartListFilters
import tv.trakt.trakt.common.model.lists.SmartListSource
import tv.trakt.trakt.common.model.lists.SmartListSource.Anticipated
import tv.trakt.trakt.common.model.lists.SmartListSource.Discover
import tv.trakt.trakt.common.model.lists.SmartListSource.Popular
import tv.trakt.trakt.common.model.lists.SmartListSource.Recommendations
import tv.trakt.trakt.common.model.lists.SmartListSource.Trending
import tv.trakt.trakt.common.model.lists.SmartListSource.Unknown
import tv.trakt.trakt.common.networking.CreateSmartListRequestDto
import tv.trakt.trakt.common.networking.SmartListFiltersDto
import tv.trakt.trakt.core.lists.sections.smart.data.local.ListsSmartLocalDataSource

internal class CreateSmartListUseCase(
    private val remoteSource: UserSmartListsRemoteDataSource,
    private val localSource: ListsSmartLocalDataSource,
) {
    suspend fun createList(
        name: String,
        filters: SmartListFilters,
    ) {
        remoteSource.createSmartList(
            request = CreateSmartListRequestDto(
                name = name.trim(),
                source = filters.source.toDto(),
                mediaType = filters.media.toMediaType(),
                filters = filters.toFiltersDto(),
            ),
        )

        with(localSource) {
            clear()
            notifyUpdate()
        }
    }
}

private fun SmartListSource.toDto(): PostUsersSmartListsCreateRequest.Source {
    return when (this) {
        Trending -> PostUsersSmartListsCreateRequest.Source.TRENDING
        Popular -> PostUsersSmartListsCreateRequest.Source.POPULAR
        Anticipated -> PostUsersSmartListsCreateRequest.Source.ANTICIPATED
        Recommendations -> PostUsersSmartListsCreateRequest.Source.RECOMMENDATIONS
        Discover -> PostUsersSmartListsCreateRequest.Source.DISCOVER
        Unknown -> throw IllegalArgumentException("Unsupported smart list source: $this")
    }
}

private fun MediaMode.toMediaType(): PostUsersSmartListsCreateRequest.MediaType {
    return when (this) {
        Shows -> PostUsersSmartListsCreateRequest.MediaType.SHOWS
        Movies -> PostUsersSmartListsCreateRequest.MediaType.MOVIES
        else -> throw IllegalArgumentException("Unsupported media mode: $this")
    }
}

private fun SmartListFilters.toFiltersDto(): SmartListFiltersDto {
    return SmartListFiltersDto(
        genres = genres?.map { it.slug }.orNullIfEmpty(),
        subgenres = subgenres.orNullIfEmpty(),
        // The create API expects a comma-separated string for certifications, so we need to split them.
        certifications = certifications?.flatMap { it.split(",") }.orNullIfEmpty(),
        languages = languages.orNullIfEmpty(),
        countries = countries.orNullIfEmpty(),
        statuses = statuses.orNullIfEmpty(),
        networks = networks.orNullIfEmpty(),
        watchnow = availability?.map { it.slug }.orNullIfEmpty(),
        years = years.orNullIfEmpty(),
        ratings = ratings.orNullIfEmpty(),
        runtimes = runtimes.orNullIfEmpty(),
        imdbRatings = imdbRatings.orNullIfEmpty(),
        rtMeters = rtMeters.orNullIfEmpty(),
        rtUserMeters = rtUserMeters.orNullIfEmpty(),
        ignoreWatched = ignoreWatched.takeIf { it },
        ignoreWatchlisted = ignoreWatchlisted.takeIf { it },
    )
}

private fun <T> Collection<T>?.orNullIfEmpty(): List<T>? = this?.takeIf { it.isNotEmpty() }?.toList()
