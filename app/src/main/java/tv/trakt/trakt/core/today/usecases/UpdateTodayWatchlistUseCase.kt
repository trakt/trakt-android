package tv.trakt.trakt.core.today.usecases

import tv.trakt.trakt.common.core.lists.model.WatchlistItem
import tv.trakt.trakt.common.core.user.data.local.watchlist.UserWatchlistLocalDataSource
import tv.trakt.trakt.common.core.user.data.local.watchlist.WatchlistUpdates
import tv.trakt.trakt.common.core.user.data.local.watchlist.WatchlistUpdates.Source.Default
import tv.trakt.trakt.common.core.user.data.local.watchlist.minimal.UserWatchlistMinimalLocalDataSource
import tv.trakt.trakt.common.core.user.usecases.lists.LoadUserWatchlistUseCase
import tv.trakt.trakt.common.helpers.extensions.nowUtcInstant
import tv.trakt.trakt.core.sync.usecases.UpdateMovieWatchlistUseCase
import tv.trakt.trakt.core.sync.usecases.UpdateShowWatchlistUseCase
import tv.trakt.trakt.core.today.model.TodayMedia

internal class UpdateTodayWatchlistUseCase(
    private val updateMovieWatchlistUseCase: UpdateMovieWatchlistUseCase,
    private val updateShowWatchlistUseCase: UpdateShowWatchlistUseCase,
    private val loadWatchlistUseCase: LoadUserWatchlistUseCase,
    private val userWatchlistLocalSource: UserWatchlistLocalDataSource,
    private val userWatchlistMinLocalSource: UserWatchlistMinimalLocalDataSource,
    private val watchlistUpdates: WatchlistUpdates,
) {
    suspend fun isWatchlisted(media: TodayMedia): Boolean {
        if (!loadWatchlistUseCase.isLoaded()) {
            loadWatchlistUseCase.loadWatchlist()
        }

        return when (media) {
            is TodayMedia.MovieMedia -> userWatchlistMinLocalSource.containsMovie(media.ids.trakt)
            is TodayMedia.ShowMedia -> userWatchlistMinLocalSource.containsShow(media.ids.trakt)
        }
    }

    suspend fun addToWatchlist(media: TodayMedia) {
        when (media) {
            is TodayMedia.MovieMedia -> {
                updateMovieWatchlistUseCase.addToWatchlist(movieId = media.ids.trakt)
                userWatchlistLocalSource.addMovies(
                    movies = listOf(
                        WatchlistItem.MovieItem(
                            rank = 0,
                            movie = media.movie,
                            listedAt = nowUtcInstant(),
                        ),
                    ),
                )
                userWatchlistMinLocalSource.addMovies(movies = setOf(media.ids.trakt))
            }
            is TodayMedia.ShowMedia -> {
                updateShowWatchlistUseCase.addToWatchlist(showId = media.ids.trakt)
                userWatchlistLocalSource.addShows(
                    shows = listOf(
                        WatchlistItem.ShowItem(
                            rank = 0,
                            show = media.show,
                            listedAt = nowUtcInstant(),
                        ),
                    ),
                )
                userWatchlistMinLocalSource.addShows(shows = setOf(media.ids.trakt))
            }
        }

        watchlistUpdates.notifyUpdate(Default)
    }

    suspend fun removeFromWatchlist(media: TodayMedia) {
        when (media) {
            is TodayMedia.MovieMedia -> {
                updateMovieWatchlistUseCase.removeFromWatchlist(movieId = media.ids.trakt)
                userWatchlistLocalSource.removeMovies(ids = setOf(media.ids.trakt))
                userWatchlistMinLocalSource.removeMovies(ids = setOf(media.ids.trakt))
            }
            is TodayMedia.ShowMedia -> {
                updateShowWatchlistUseCase.removeFromWatchlist(showId = media.ids.trakt)
                userWatchlistLocalSource.removeShows(ids = setOf(media.ids.trakt))
                userWatchlistMinLocalSource.removeShows(ids = setOf(media.ids.trakt))
            }
        }

        watchlistUpdates.notifyUpdate(Default)
    }
}
