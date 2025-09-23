package tv.trakt.trakt.core.user.usecase.progress

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import tv.trakt.trakt.common.helpers.extensions.asyncMap
import tv.trakt.trakt.common.helpers.extensions.toInstant
import tv.trakt.trakt.common.model.Movie
import tv.trakt.trakt.common.model.Show
import tv.trakt.trakt.common.model.fromDto
import tv.trakt.trakt.core.sync.model.ProgressItem
import tv.trakt.trakt.core.user.data.local.UserProgressLocalDataSource
import tv.trakt.trakt.core.user.data.remote.UserRemoteDataSource

internal class LoadUserProgressUseCase(
    private val remoteSource: UserRemoteDataSource,
    private val localSource: UserProgressLocalDataSource,
) {
    suspend fun loadLocalAll(): ImmutableList<ProgressItem> {
        return localSource.getAll()
            .toImmutableList()
    }

    suspend fun loadLocalShows(): ImmutableList<ProgressItem.ShowItem> {
        return localSource.getShows()
            .toImmutableList()
    }

    suspend fun loadLocalMovies(): ImmutableList<ProgressItem.MovieItem> {
        return localSource.getMovies()
            .toImmutableList()
    }

    suspend fun isLoaded(): Boolean {
        return localSource.isMoviesLoaded() && localSource.isShowsLoaded()
    }

    suspend fun loadMoviesProgress(): ImmutableList<ProgressItem.MovieItem> {
        val response = remoteSource.getWatchedMovies()
            .asyncMap {
                ProgressItem.MovieItem(
                    plays = it.plays,
                    movie = Movie.fromDto(it.movie),
                )
            }

        with(localSource) {
            setMovies(response)
        }

        return response.toImmutableList()
    }

    suspend fun loadShowsProgress(limit: Int? = null): ImmutableList<ProgressItem.ShowItem> {
        val response = remoteSource.getWatchedShows(limit = limit)
            .asyncMap {
                ProgressItem.ShowItem(
                    show = Show.fromDto(it.show),
                    progress = ProgressItem.ShowItem.Progress(
                        aired = it.progress.aired,
                        completed = it.progress.completed,
                        lastWatchedAt = it.progress.lastWatchedAt?.toInstant(),
                        resetAt = it.progress.resetAt?.toInstant(),
                    ),
                )
            }

        with(localSource) {
            when (limit) {
                null -> setShows(response)
                else -> addShows(response)
            }
        }

        return response.toImmutableList()
    }

    suspend fun loadAllProgress() {
        return coroutineScope {
            val showsAsync = async { loadShowsProgress() }
            val moviesAsync = async { loadMoviesProgress() }

            showsAsync.await()
            moviesAsync.await()
        }
    }
}
