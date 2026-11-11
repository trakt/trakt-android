package tv.trakt.trakt.core.today.usecases

import tv.trakt.trakt.common.core.today.model.TodayMedia
import tv.trakt.trakt.common.core.user.usecases.lists.LoadUserWatchlistUseCase
import tv.trakt.trakt.common.data.local.UserWatchlistMinimalLocalSource
import tv.trakt.trakt.core.sync.usecases.UpdateMovieWatchlistUseCase
import tv.trakt.trakt.core.sync.usecases.UpdateShowWatchlistUseCase

internal class UpdateTodayWatchlistUseCase(
    private val updateMovieWatchlistUseCase: UpdateMovieWatchlistUseCase,
    private val updateShowWatchlistUseCase: UpdateShowWatchlistUseCase,
    private val loadWatchlistUseCase: LoadUserWatchlistUseCase,
    private val userWatchlistMinLocalSource: UserWatchlistMinimalLocalSource,
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
                updateMovieWatchlistUseCase.addToWatchlist(media.movie)
            }
            is TodayMedia.ShowMedia -> {
                updateShowWatchlistUseCase.addToWatchlist(media.show)
            }
        }
    }

    suspend fun removeFromWatchlist(media: TodayMedia) {
        when (media) {
            is TodayMedia.MovieMedia -> {
                updateMovieWatchlistUseCase.removeFromWatchlist(movieId = media.ids.trakt)
            }
            is TodayMedia.ShowMedia -> {
                updateShowWatchlistUseCase.removeFromWatchlist(showId = media.ids.trakt)
            }
        }
    }
}
