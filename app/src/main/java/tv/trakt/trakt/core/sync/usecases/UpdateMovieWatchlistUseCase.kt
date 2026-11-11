package tv.trakt.trakt.core.sync.usecases

import tv.trakt.trakt.common.core.lists.model.WatchlistItem
import tv.trakt.trakt.common.core.sync.changes.DataChange.Kind
import tv.trakt.trakt.common.core.sync.changes.DataChanges
import tv.trakt.trakt.common.data.local.UserWatchlistLocalSource
import tv.trakt.trakt.common.data.local.UserWatchlistMinimalLocalSource
import tv.trakt.trakt.common.data.remote.MoviesSyncRemoteSource
import tv.trakt.trakt.common.helpers.extensions.nowUtcInstant
import tv.trakt.trakt.common.model.Movie
import tv.trakt.trakt.common.model.TraktId

internal class UpdateMovieWatchlistUseCase(
    private val remoteSource: MoviesSyncRemoteSource,
    private val widgetsUpdater: tv.trakt.trakt.widgets.widget.calendar.CalendarWidgetUpdater,
    private val userWatchlistLocalSource: UserWatchlistLocalSource,
    private val userWatchlistMinLocalSource: UserWatchlistMinimalLocalSource,
    private val dataChanges: DataChanges,
) {
    suspend fun addToWatchlist(movie: Movie) {
        val movieId = movie.ids.trakt

        remoteSource.addToWatchlist(
            movieId = movieId,
        )
        widgetsUpdater.refreshInBackground()

        userWatchlistLocalSource.addMovies(
            movies = listOf(
                WatchlistItem.MovieItem(
                    rank = 0,
                    movie = movie,
                    listedAt = nowUtcInstant(),
                ),
            ),
        )
        userWatchlistMinLocalSource.addMovies(movies = setOf(movieId))
        dataChanges.notify(Kind.Watchlist)
    }

    suspend fun removeFromWatchlist(movieId: TraktId) {
        remoteSource.removeFromWatchlist(
            movieId = movieId,
        )
        widgetsUpdater.refreshInBackground()

        userWatchlistLocalSource.removeMovies(ids = setOf(movieId))
        userWatchlistMinLocalSource.removeMovies(ids = setOf(movieId))
        dataChanges.notify(Kind.Watchlist)
    }
}
