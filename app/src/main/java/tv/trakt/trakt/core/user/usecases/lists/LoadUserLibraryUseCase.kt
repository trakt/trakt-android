package tv.trakt.trakt.core.user.usecases.lists

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import tv.trakt.trakt.common.helpers.extensions.EmptyImmutableList
import tv.trakt.trakt.common.helpers.extensions.asyncMap
import tv.trakt.trakt.common.helpers.extensions.toInstant
import tv.trakt.trakt.common.model.Episode
import tv.trakt.trakt.common.model.Movie
import tv.trakt.trakt.common.model.Show
import tv.trakt.trakt.common.model.fromDto
import tv.trakt.trakt.core.library.model.LibraryItem
import tv.trakt.trakt.core.user.data.local.library.UserLibraryLocalDataSource
import tv.trakt.trakt.core.user.data.remote.UserRemoteDataSource

/**
 * Loads the user's favorites from the remote source and updates the local cache.
 */
internal class LoadUserLibraryUseCase(
    private val remoteSource: UserRemoteDataSource,
    private val localSource: UserLibraryLocalDataSource,
) {
    suspend fun isLoaded(): Boolean {
        return localSource.isLoaded()
    }

    suspend fun loadLocalMedia(limit: Int): Result {
        val items = localSource.getAll().toImmutableList()
        return Result(
            items = items,
            hasMoreData = items.size >= limit,
        )
    }

    suspend fun loadMedia(
        page: Int,
        limit: Int,
        availableOn: String?,
    ): Result {
        val items = remoteSource.getLibrary(
            extended = "full,images,colors,available_on",
            availableOn = availableOn,
            page = page,
            limit = limit,
        )
            .filter { it.movie != null || (it.episode != null && it.show != null) }
            .asyncMap { item ->
                item.movie?.let {
                    LibraryItem.MovieItem(
                        movie = Movie.fromDto(it),
                        collectedAt = item.collectedAt.toInstant(),
                        updatedAt = item.updatedAt.toInstant(),
                        availableOn = item.availableOn
                            ?.map { source -> source.name }
                            ?.toImmutableList()
                            ?: EmptyImmutableList,
                    )
                } ?: item.episode?.let {
                    LibraryItem.EpisodeItem(
                        episode = Episode.fromDto(it),
                        show = Show.fromDto(item.show!!),
                        collectedAt = item.collectedAt.toInstant(),
                        updatedAt = item.updatedAt.toInstant(),
                        availableOn = item.availableOn
                            ?.map { source -> source.name }
                            ?.toImmutableList()
                            ?: EmptyImmutableList,
                    )
                } ?: throw Error("Unknown library item type")
            }
            .also {
                when {
                    page == 1 -> localSource.setItems(it)
                    else -> localSource.addItems(it)
                }
            }
            .toImmutableList()

        return Result(
            items = items,
            hasMoreData = items.size >= limit,
        )
    }

    data class Result(
        val items: ImmutableList<LibraryItem>,
        val hasMoreData: Boolean,
    )
}
