package tv.trakt.trakt.core.user.usecases.lists

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
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
    suspend fun loadLocalAll(): ImmutableList<LibraryItem> {
        return localSource.getAll()
            .sortedByDescending { it.collectedAt }
            .toImmutableList()
    }

    suspend fun loadLocalEpisodes(): ImmutableList<LibraryItem.EpisodeItem> {
        return localSource.getEpisodes()
            .sortedByDescending { it.collectedAt }
            .toImmutableList()
    }

    suspend fun loadLocalMovies(): ImmutableList<LibraryItem.MovieItem> {
        return localSource.getMovies()
            .sortedByDescending { it.collectedAt }
            .toImmutableList()
    }

    suspend fun isEpisodesLoaded(): Boolean {
        return localSource.isEpisodesLoaded()
    }

    suspend fun isMoviesLoaded(): Boolean {
        return localSource.isMoviesLoaded()
    }

    suspend fun loadAll(): ImmutableList<LibraryItem> {
        return coroutineScope {
            val episodesAsync = async { loadEpisodes() }
            val moviesAsync = async { loadMovies() }

            val episodes = episodesAsync.await()
            val movies = moviesAsync.await()

            (episodes + movies)
                .sortedByDescending { it.collectedAt }
                .toImmutableList()
        }
    }

    suspend fun loadMovies(): ImmutableList<LibraryItem> {
        return remoteSource.getLibraryMovies(
            extended = "full,images,available_on",
        ).asyncMap {
            LibraryItem.MovieItem(
                movie = Movie.fromDto(it.movie),
                collectedAt = it.collectedAt.toInstant(),
                updatedAt = it.updatedAt.toInstant(),
                availableOn = it.availableOn
                    ?.map { source -> source.name }
                    ?.toImmutableList()
                    ?: EmptyImmutableList,
            )
        }
            .sortedByDescending { it.collectedAt }
            .also { localSource.setMovies(it) }
            .toImmutableList()
    }

    suspend fun loadEpisodes(): ImmutableList<LibraryItem> {
        return remoteSource.getLibraryEpisodes(
            extended = "full,images,available_on",
        ).asyncMap {
            LibraryItem.EpisodeItem(
                episode = Episode.fromDto(it.episode),
                show = Show.fromDto(it.show),
                collectedAt = it.collectedAt.toInstant(),
                updatedAt = it.updatedAt.toInstant(),
                availableOn = it.availableOn
                    ?.map { source -> source.name }
                    ?.toImmutableList()
                    ?: EmptyImmutableList,
            )
        }
            .sortedByDescending { it.collectedAt }
            .also { localSource.setEpisodes(it) }
            .toImmutableList()
    }
}
