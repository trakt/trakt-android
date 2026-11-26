package tv.trakt.trakt.core.mixtape.usecases

import tv.trakt.trakt.common.model.mixtape.MixtapeItem
import tv.trakt.trakt.core.sync.usecases.UpdateMovieWatchlistUseCase
import tv.trakt.trakt.core.sync.usecases.UpdateShowWatchlistUseCase

internal class AddMixtapeToWatchlistUseCase(
    private val updateMovieWatchlistUseCase: UpdateMovieWatchlistUseCase,
    private val updateShowWatchlistUseCase: UpdateShowWatchlistUseCase,
) {
    suspend fun addToWatchlist(item: MixtapeItem) {
        when (item) {
            is MixtapeItem.MovieItem -> updateMovieWatchlistUseCase.addToWatchlist(item.movie)
            is MixtapeItem.ShowItem -> updateShowWatchlistUseCase.addToWatchlist(item.show)
        }
    }
}
