package tv.trakt.trakt.core.calendar.usecases

import tv.trakt.trakt.common.core.user.usecases.progress.LoadUserProgressUseCase
import tv.trakt.trakt.common.firebase.analytics.Analytics
import tv.trakt.trakt.common.model.DateSelectionResult
import tv.trakt.trakt.common.model.Episode
import tv.trakt.trakt.common.model.Movie
import tv.trakt.trakt.core.ratings.rateprompt.RatePromptManager
import tv.trakt.trakt.core.summary.episodes.data.EpisodeDetailsUpdates
import tv.trakt.trakt.core.summary.episodes.data.EpisodeDetailsUpdates.Source.Calendar
import tv.trakt.trakt.core.sync.usecases.UpdateEpisodeHistoryUseCase
import tv.trakt.trakt.core.sync.usecases.UpdateMovieHistoryUseCase

private const val ANALYTICS_SOURCE = "calendar"
private const val ANALYTICS_EPISODE = "episode"
private const val ANALYTICS_MOVIE = "movie"

/**
 * Watched-state changes made from a calendar item: history write, progress reload,
 * update broadcast and analytics. Shared by the weekly and monthly calendars.
 */
internal class UpdateCalendarHistoryUseCase(
    private val updateEpisodeHistoryUseCase: UpdateEpisodeHistoryUseCase,
    private val updateMovieHistoryUseCase: UpdateMovieHistoryUseCase,
    private val loadUserProgressUseCase: LoadUserProgressUseCase,
    private val ratePromptManager: RatePromptManager,
    private val episodeUpdates: EpisodeDetailsUpdates,
    private val analytics: Analytics,
) {
    suspend fun addToHistory(
        episode: Episode,
        customDate: DateSelectionResult? = null,
    ) {
        updateEpisodeHistoryUseCase.addToHistory(
            episodeId = episode.ids.trakt,
            customDate = customDate,
        )
        loadUserProgressUseCase.loadShowsProgress()
        episodeUpdates.notifyUpdate(Calendar)

        analytics.progress.logAddWatchedMedia(
            mediaType = ANALYTICS_EPISODE,
            source = ANALYTICS_SOURCE,
            date = customDate?.analyticsStrings,
        )
    }

    suspend fun addToHistory(
        movie: Movie,
        customDate: DateSelectionResult? = null,
    ) {
        updateMovieHistoryUseCase.addToWatched(
            movieId = movie.ids.trakt,
            customDate = customDate,
        )
        loadUserProgressUseCase.loadMoviesProgress()
        episodeUpdates.notifyUpdate(Calendar)

        analytics.progress.logAddWatchedMedia(
            mediaType = ANALYTICS_MOVIE,
            source = ANALYTICS_SOURCE,
            date = customDate?.analyticsStrings,
        )

        ratePromptManager.checkMovies()
    }

    suspend fun removeFromWatched(episode: Episode) {
        updateEpisodeHistoryUseCase.removeEpisodeFromHistory(episode.ids.trakt.value)
        loadUserProgressUseCase.loadShowsProgress()
        episodeUpdates.notifyUpdate(Calendar)

        analytics.progress.logRemoveWatchedMedia(
            mediaType = ANALYTICS_EPISODE,
            source = ANALYTICS_SOURCE,
        )
    }

    suspend fun removeFromWatched(movie: Movie) {
        updateMovieHistoryUseCase.removeAllFromHistory(movie.ids.trakt)
        loadUserProgressUseCase.loadMoviesProgress()
        episodeUpdates.notifyUpdate(Calendar)

        analytics.progress.logRemoveWatchedMedia(
            mediaType = ANALYTICS_MOVIE,
            source = ANALYTICS_SOURCE,
        )
    }
}
