package tv.trakt.trakt.core.home.sections.watchlist.usecases

import tv.trakt.trakt.common.core.sync.changes.ChangeOrigin
import tv.trakt.trakt.common.data.local.UserWatchlistLocalSource
import tv.trakt.trakt.common.data.local.UserWatchlistMinimalLocalSource
import tv.trakt.trakt.common.model.DateSelectionResult
import tv.trakt.trakt.common.model.SeasonEpisode
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.core.sync.usecases.UpdateEpisodeHistoryUseCase
import tv.trakt.trakt.core.sync.usecases.UpdateMovieHistoryUseCase

internal class AddHomeHistoryUseCase(
    private val updateMovieHistoryUseCase: UpdateMovieHistoryUseCase,
    private val updateEpisodeHistoryUseCase: UpdateEpisodeHistoryUseCase,
) {
    suspend fun addMovieToHistory(
        movieId: TraktId,
        customDate: DateSelectionResult? = null,
        origin: ChangeOrigin? = null,
    ) {
        updateMovieHistoryUseCase.addToWatched(
            movieId = movieId,
            customDate = customDate,
            origin = origin,
        )
    }

    suspend fun addEpisodeToHistory(
        showId: TraktId,
        seasonEpisode: SeasonEpisode,
        customDate: DateSelectionResult? = null,
        origin: ChangeOrigin? = null,
    ) {
        updateEpisodeHistoryUseCase.addToHistory(
            showId = showId,
            seasonEpisode = seasonEpisode,
            customDate = customDate,
            origin = origin,
            refreshProgress = false,
        )
    }
}
