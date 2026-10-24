package tv.trakt.trakt.core.calendar.usecases

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.persistentMapOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.collections.immutable.toImmutableMap
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import tv.trakt.trakt.common.auth.session.SessionManager
import tv.trakt.trakt.common.core.sync.model.ProgressItem
import tv.trakt.trakt.common.core.user.usecases.progress.LoadUserProgressUseCase
import tv.trakt.trakt.common.data.remote.UserCalendarRemoteSource
import tv.trakt.trakt.common.helpers.extensions.asyncMap
import tv.trakt.trakt.common.helpers.extensions.toLocalDay
import tv.trakt.trakt.common.model.Episode
import tv.trakt.trakt.common.model.Movie
import tv.trakt.trakt.common.model.Show
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.model.calendar.CalendarItem
import tv.trakt.trakt.common.model.fromDto
import tv.trakt.trakt.common.model.globalfilter.GlobalFilter
import tv.trakt.trakt.core.calendar.model.CalendarSource
import tv.trakt.trakt.core.discover.sections.releases.model.ReleaseType
import tv.trakt.trakt.core.discover.sections.releases.usecases.movies.GetReleasesMoviesUseCase
import tv.trakt.trakt.core.discover.sections.releases.usecases.shows.GetReleasesShowsUseCase
import java.time.LocalDate
import java.time.ZoneOffset.UTC
import java.time.temporal.ChronoUnit

// The calendar endpoints key off release dates in UTC, so the request window gets one
// day of padding on each side: a release on a local range-end evening lands on the next
// UTC day in negative-offset zones, and on the previous UTC day for local range-start
// mornings in positive-offset ones. The local-day filter below trims the excess.
private const val DAYS_OFFSET = 1L

// Trakt caps a single calendar call at 44 days, enough for a padded 6-row month page.
// Longer ranges are split into consecutive windows and fetched in parallel.
private const val MAX_REQUEST_DAYS = 44

/**
 * Loads calendar items for an arbitrary day range and returns them grouped by day,
 * with an entry for every day in the range - empty days included.
 *
 * Shared by the weekly and monthly calendar use cases, for both calendar sources.
 */
internal class CalendarItemsLoader(
    private val sessionManager: SessionManager,
    private val loadUserProgressUseCase: LoadUserProgressUseCase,
    private val remoteUserSource: UserCalendarRemoteSource,
    private val getReleasesShowsUseCase: GetReleasesShowsUseCase,
    private val getReleasesMoviesUseCase: GetReleasesMoviesUseCase,
) {
    suspend fun load(
        source: CalendarSource,
        range: ClosedRange<LocalDate>,
        filters: GlobalFilter,
        type: ReleaseType,
        skipProgress: Boolean = false,
    ): ImmutableMap<LocalDate, ImmutableList<CalendarItem>> {
        val authenticated = sessionManager.isAuthenticated()
        if (source.requiresAuth && !authenticated) {
            return persistentMapOf()
        }

        return coroutineScope {
            val windows = requestWindows(range)

            val itemsAsync = async {
                when (source) {
                    CalendarSource.Personal -> loadPersonalItems(windows, filters, type)
                    CalendarSource.Releases -> loadReleasesItems(windows, filters, type)
                }
            }

            val loadProgress = authenticated && !skipProgress

            val showsProgressAsync = async {
                if (!loadProgress) return@async emptyList()
                with(loadUserProgressUseCase) {
                    when {
                        isShowsLoaded() -> loadLocalShows()
                        else -> loadShowsProgress()
                    }
                }
            }

            val moviesProgressAsync = async {
                if (!loadProgress) return@async emptyList()
                with(loadUserProgressUseCase) {
                    when {
                        isMoviesLoaded() -> loadLocalMovies()
                        else -> loadMoviesProgress()
                    }
                }
            }

            val items = itemsAsync.await()
            val showsProgress = showsProgressAsync.await()
                .associateBy { it.showId }
            val watchedMovies = moviesProgressAsync.await()
                .map { it.movie.ids.trakt }
                .toSet()

            val itemsByDay = items
                .distinctBy { it.key }
                .mapNotNull { item ->
                    val day = item.releasedAt?.toLocalDay() ?: return@mapNotNull null
                    if (day !in range) return@mapNotNull null
                    day to item.withWatched(showsProgress, watchedMovies)
                }
                .groupBy(
                    keySelector = { (day, _) -> day },
                    valueTransform = { (_, item) -> item },
                )

            val days = ChronoUnit.DAYS.between(range.start, range.endInclusive) + 1
            val result = buildMap<LocalDate, ImmutableList<CalendarItem>> {
                for (offset in 0L until days) {
                    val currentDay = range.start.plusDays(offset)
                    val dayItems = (itemsByDay[currentDay] ?: emptyList())
                        .sortedBy { it.releasedAt }
                        .toImmutableList()
                    put(currentDay, dayItems)
                }
            }

            result.toImmutableMap()
        }
    }

    private suspend fun loadPersonalItems(
        windows: List<CalendarRequestWindow>,
        filters: GlobalFilter,
        type: ReleaseType,
    ): List<CalendarItem> {
        val mediaData = windows.asyncMap {
            remoteUserSource.getMediaCalendar(
                startDate = it.startDate,
                days = it.days,
                filters = filters,
            )
        }.flatten()

        val episodes = mediaData
            .mapNotNull { item ->
                val show = item.show ?: return@mapNotNull null
                val episode = item.episode ?: return@mapNotNull null
                if (episode.season <= 0) return@mapNotNull null
                show to Episode.fromDto(episode)
            }
            // Group a show's episodes that share the same release day into one item,
            // so a same-day batch renders as a single card with a combined list.
            .groupBy { (show, episode) -> show.ids.trakt to episode.releasedAt?.toLocalDay() }
            .map { (_, entries) ->
                val episodeModels = entries
                    .map { (_, episode) -> episode }
                    .sortedBy { it.number }
                    .toImmutableList()

                CalendarItem.EpisodeItem(
                    episodes = episodeModels,
                    show = Show.fromDto(entries.first().first),
                    isFullSeason = episodeModels.isFullSeason(),
                )
            }
            .filter { item -> item.isReleaseType(type) }

        // Release type applies to episodes only; movies show for All only.
        if (type != ReleaseType.All) {
            return episodes
        }

        val movies = mediaData.mapNotNull { item ->
            val movie = item.movie ?: return@mapNotNull null
            if (item.released == null) return@mapNotNull null
            CalendarItem.MovieItem(movie = Movie.fromDto(movie))
        }

        return episodes + movies
    }

    private suspend fun loadReleasesItems(
        windows: List<CalendarRequestWindow>,
        filters: GlobalFilter,
        type: ReleaseType,
    ): List<CalendarItem> {
        return coroutineScope {
            val showsAsync = async {
                if (!filters.mode.isMediaOrShows) return@async emptyList()
                windows.asyncMap {
                    getReleasesShowsUseCase.getShows(
                        startDate = it.startDate.atStartOfDay(UTC).toInstant(),
                        days = it.days,
                        filters = filters,
                        skipLocal = true,
                        type = type,
                    )
                }.flatten()
            }

            val moviesAsync = async {
                // Release type applies to episodes only; movies are excluded for Finale.
                if (type == ReleaseType.Finale || !filters.mode.isMediaOrMovies) {
                    return@async emptyList()
                }
                windows.asyncMap {
                    getReleasesMoviesUseCase.getMovies(
                        startDate = it.startDate.atStartOfDay(UTC).toInstant(),
                        days = it.days,
                        filters = filters,
                        skipLocal = true,
                    )
                }.flatten()
            }

            showsAsync.await() + moviesAsync.await()
        }
    }
}

private fun CalendarItem.withWatched(
    showsProgress: Map<TraktId, ProgressItem.ShowItem>,
    watchedMovies: Set<TraktId>,
): CalendarItem {
    return when (this) {
        is CalendarItem.EpisodeItem -> copy(
            watched = showsProgress[show.ids.trakt]?.isEpisodeWatched(
                seasonNumber = episode.season,
                episodeId = episode.ids.trakt,
            ) == true,
        )

        is CalendarItem.MovieItem -> copy(
            watched = movie.ids.trakt in watchedMovies,
        )
    }
}

private data class CalendarRequestWindow(
    val startDate: LocalDate,
    val days: Int,
)

// Consecutive windows of at most MAX_REQUEST_DAYS days covering the padded range.
private fun requestWindows(range: ClosedRange<LocalDate>): List<CalendarRequestWindow> {
    val startDate = range.start.minusDays(DAYS_OFFSET)
    val days = ChronoUnit.DAYS.between(range.start, range.endInclusive).toInt() + 1 +
        2 * DAYS_OFFSET.toInt()
    val windowCount = (days + MAX_REQUEST_DAYS - 1) / MAX_REQUEST_DAYS

    return List(windowCount) { index ->
        val offset = index * MAX_REQUEST_DAYS
        CalendarRequestWindow(
            startDate = startDate.plusDays(offset.toLong()),
            days = minOf(MAX_REQUEST_DAYS, days - offset),
        )
    }
}

// A same-day batch is a full season when it spans more than one episode and
// carries both the season premiere and the season finale.
private fun List<Episode>.isFullSeason(): Boolean {
    if (size <= 1) return false
    val hasPremiere = any { it.type?.isPremiere == true }
    val hasFinale = any { it.type?.isFinale == true }
    return hasPremiere && hasFinale
}

// A grouped episode item matches Premiere/Finale when any of its episodes carries
// that type; All matches everything.
private fun CalendarItem.EpisodeItem.isReleaseType(type: ReleaseType): Boolean {
    return when (type) {
        ReleaseType.All -> true
        ReleaseType.Premiere -> episodes.any { it.type?.isPremiere == true }
        ReleaseType.Finale -> episodes.any { it.type?.isFinale == true }
    }
}
