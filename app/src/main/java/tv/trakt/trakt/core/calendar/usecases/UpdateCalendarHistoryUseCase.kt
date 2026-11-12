package tv.trakt.trakt.core.calendar.usecases

import tv.trakt.trakt.common.core.sync.changes.ChangeOrigin
import tv.trakt.trakt.common.firebase.analytics.Analytics
import tv.trakt.trakt.common.model.DateSelectionResult
import tv.trakt.trakt.common.model.Episode
import tv.trakt.trakt.common.model.Movie
import tv.trakt.trakt.core.calendar.model.CalendarSource
import tv.trakt.trakt.core.ratings.rateprompt.RatePromptManager
import tv.trakt.trakt.core.sync.usecases.UpdateEpisodeHistoryUseCase
import tv.trakt.trakt.core.sync.usecases.UpdateMovieHistoryUseCase

private const val ANALYTICS_EPISODE = "episode"
private const val ANALYTICS_MOVIE = "movie"

/**
 * Watched-state changes made from a calendar item: history write and analytics.
 * Shared by the weekly and monthly calendars.
 */
internal class UpdateCalendarHistoryUseCase(
    private val updateEpisodeHistoryUseCase: UpdateEpisodeHistoryUseCase,
    private val updateMovieHistoryUseCase: UpdateMovieHistoryUseCase,
    private val ratePromptManager: RatePromptManager,
    private val analytics: Analytics,
) {
    suspend fun addToHistory(
        episode: Episode,
        source: CalendarSource,
        origin: ChangeOrigin,
        customDate: DateSelectionResult? = null,
    ) {
        updateEpisodeHistoryUseCase.addToHistory(
            episodeId = episode.ids.trakt,
            customDate = customDate,
            origin = origin,
        )

        analytics.progress.logAddWatchedMedia(
            mediaType = ANALYTICS_EPISODE,
            source = source.analyticsSource,
            date = customDate?.analyticsStrings,
        )
    }

    suspend fun addToHistory(
        movie: Movie,
        source: CalendarSource,
        origin: ChangeOrigin,
        customDate: DateSelectionResult? = null,
    ) {
        updateMovieHistoryUseCase.addToWatched(
            movieId = movie.ids.trakt,
            customDate = customDate,
            origin = origin,
        )

        analytics.progress.logAddWatchedMedia(
            mediaType = ANALYTICS_MOVIE,
            source = source.analyticsSource,
            date = customDate?.analyticsStrings,
        )

        ratePromptManager.checkRecentlyWatched()
    }
}
