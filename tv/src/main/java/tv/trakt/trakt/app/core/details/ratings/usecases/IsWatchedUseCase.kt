package tv.trakt.trakt.app.core.details.ratings.usecases

import kotlinx.collections.immutable.ImmutableList
import tv.trakt.trakt.common.core.sync.model.ProgressItem
import tv.trakt.trakt.common.core.user.usecases.progress.LoadUserProgressUseCase
import tv.trakt.trakt.common.model.TraktId

internal class IsWatchedUseCase(
    private val loadUserProgressUseCase: LoadUserProgressUseCase,
) {
    suspend fun isMovieWatched(movieId: TraktId): Boolean {
        return loadMovies().any { it.movie.ids.trakt == movieId && it.plays.isNotEmpty() }
    }

    suspend fun isShowWatched(showId: TraktId): Boolean {
        return loadShows().any { it.showId == showId && it.plays > 0 }
    }

    suspend fun isEpisodeWatched(
        showId: TraktId,
        episodeId: TraktId,
    ): Boolean {
        return loadShows()
            .firstOrNull { it.showId == showId }
            ?.seasons
            ?.any { season -> season.episodes.any { it.id == episodeId && it.plays.isNotEmpty() } }
            ?: false
    }

    private suspend fun loadMovies(): ImmutableList<ProgressItem.MovieItem> {
        with(loadUserProgressUseCase) {
            return if (isMoviesLoaded()) loadLocalMovies() else loadMoviesProgress(notifyUpdate = false)
        }
    }

    private suspend fun loadShows(): ImmutableList<ProgressItem.ShowItem> {
        with(loadUserProgressUseCase) {
            return if (isShowsLoaded()) loadLocalShows() else loadShowsProgress(notifyUpdate = false)
        }
    }
}
