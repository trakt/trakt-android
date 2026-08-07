package tv.trakt.trakt.core.calendar.usecases

import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge
import tv.trakt.trakt.core.summary.episodes.data.EpisodeDetailsUpdates
import tv.trakt.trakt.core.summary.episodes.data.EpisodeDetailsUpdates.Source.History
import tv.trakt.trakt.core.summary.episodes.data.EpisodeDetailsUpdates.Source.Progress
import tv.trakt.trakt.core.summary.episodes.data.EpisodeDetailsUpdates.Source.Season
import tv.trakt.trakt.core.summary.movies.data.MovieDetailsUpdates
import tv.trakt.trakt.core.summary.shows.data.ShowDetailsUpdates
import tv.trakt.trakt.core.summary.shows.data.ShowDetailsUpdates.Source
import kotlin.time.Duration.Companion.milliseconds

/**
 * Emits whenever watched state changes somewhere else in the app and the calendar
 * needs a refresh. Shared by the weekly and monthly calendars.
 */
@OptIn(FlowPreview::class)
internal class ObserveCalendarUpdatesUseCase(
    private val showUpdates: ShowDetailsUpdates,
    private val episodeUpdates: EpisodeDetailsUpdates,
    private val movieUpdates: MovieDetailsUpdates,
) {
    fun observeUpdates(): Flow<Unit> {
        return merge(
            showUpdates.observeUpdates(Source.Progress),
            showUpdates.observeUpdates(Source.Seasons),
            showUpdates.observeUpdates(Source.WatchedUntil),
            episodeUpdates.observeUpdates(Progress),
            episodeUpdates.observeUpdates(Season),
            episodeUpdates.observeUpdates(History),
            movieUpdates.observeUpdates(MovieDetailsUpdates.Source.Progress),
            movieUpdates.observeUpdates(MovieDetailsUpdates.Source.History),
        )
            .distinctUntilChanged()
            .debounce(200.milliseconds)
            .map { }
    }
}
