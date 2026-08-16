package tv.trakt.trakt.core.calendar.usecases

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.toImmutableList
import kotlinx.collections.immutable.toImmutableMap
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import tv.trakt.trakt.common.core.user.data.remote.calendar.UserCalendarRemoteDataSource
import tv.trakt.trakt.common.core.user.usecases.progress.LoadUserProgressUseCase
import tv.trakt.trakt.common.helpers.extensions.asyncMap
import tv.trakt.trakt.common.helpers.extensions.toInstant
import tv.trakt.trakt.common.helpers.extensions.toLocalDay
import tv.trakt.trakt.common.model.Episode
import tv.trakt.trakt.common.model.Movie
import tv.trakt.trakt.common.model.Show
import tv.trakt.trakt.common.model.fromDto
import tv.trakt.trakt.common.model.globalfilter.GlobalFilter
import tv.trakt.trakt.common.model.toTraktId
import tv.trakt.trakt.core.calendar.model.CalendarItem
import tv.trakt.trakt.core.discover.sections.releases.model.ReleaseType
import java.time.LocalDate
import java.time.ZoneOffset.UTC
import java.time.temporal.ChronoUnit

// The calendar endpoints key off release dates in UTC, so the window starts a day
// early and the results are trimmed back to the requested range locally.
private const val DAYS_OFFSET = 1L

// Trakt caps a single calendar call at 33 days; longer ranges are split into
// consecutive windows and fetched in parallel.
private const val MAX_REQUEST_DAYS = 33

/**
 * Loads calendar items for an arbitrary day range and returns them grouped by day,
 * with an entry for every day in the range - empty days included.
 *
 * Shared by the weekly and monthly calendar use cases.
 */
internal class CalendarItemsLoader(
    private val loadUserProgressUseCase: LoadUserProgressUseCase,
    private val remoteUserSource: UserCalendarRemoteDataSource,
) {
    suspend fun load(
        range: ClosedRange<LocalDate>,
        filters: GlobalFilter,
        type: ReleaseType,
        skipProgress: Boolean = false,
    ): ImmutableMap<LocalDate, ImmutableList<CalendarItem>> {
        return coroutineScope {
            val startDate = range.start
            val endDate = range.endInclusive

            val days = ChronoUnit.DAYS.between(startDate, endDate).toInt() + 1 + DAYS_OFFSET.toInt()
            val windows = requestWindows(
                startDate = startDate.minusDays(DAYS_OFFSET),
                days = days,
            )

            val showsDataAsync = if (filters.mode.isMediaOrShows) {
                async {
                    windows.asyncMap {
                        remoteUserSource.getShowsCalendar(
                            startDate = it.startDate,
                            days = it.days,
                            filters = filters,
                        )
                    }.flatten()
                }
            } else {
                null
            }
            val moviesDataAsync = if (filters.mode.isMediaOrMovies) {
                async {
                    windows.asyncMap {
                        remoteUserSource.getMoviesCalendar(
                            startDate = it.startDate,
                            days = it.days,
                            filters = filters,
                        )
                    }.flatten()
                }
            } else {
                null
            }

            val showsProgressAsync = async {
                if (skipProgress) {
                    emptyList()
                } else {
                    with(loadUserProgressUseCase) {
                        when {
                            isShowsLoaded() -> loadLocalShows()
                            else -> loadShowsProgress()
                        }
                    }
                }
            }

            val moviesProgressAsync = async {
                if (skipProgress) {
                    emptyList()
                } else {
                    with(loadUserProgressUseCase) {
                        when {
                            isMoviesLoaded() -> loadLocalMovies()
                            else -> loadMoviesProgress()
                        }
                    }
                }
            }

            val showsData = showsDataAsync?.await().orEmpty()
            val moviesData = moviesDataAsync?.await().orEmpty()

            val showsProgress = showsProgressAsync.await()
                .associateBy { it.showId }
            val moviesProgress = moviesProgressAsync.await()
                .associateBy { it.movie.ids.trakt }

            val rangeShowsData = showsData.filter {
                val date = it.episode.effectiveReleaseDate ?: it.firstAired
                val localDate = date.toInstant().toLocalDay()
                it.episode.season > 0 && localDate in startDate..endDate
            }

            val episodes = rangeShowsData
                .map { it.show to Episode.fromDto(it.episode) }
                // Group a show's episodes that share the same release day into one item,
                // so a same-day batch renders as a single card with a combined list.
                .groupBy { (show, episode) -> show.ids.trakt to episode.releasedAt?.toLocalDay() }
                .map { (_, entries) ->
                    val show = Show.fromDto(entries.first().first)
                    val episodeModels = entries
                        .map { (_, episode) -> episode }
                        .sortedBy { it.number }
                        .toImmutableList()
                    val firstEpisode = episodeModels.first()

                    CalendarItem.EpisodeItem(
                        watched = showsProgress[show.ids.trakt]?.isEpisodeWatched(
                            seasonNumber = firstEpisode.season,
                            episodeId = firstEpisode.ids.trakt,
                        ) == true,
                        episodes = episodeModels,
                        show = show,
                        isFullSeason = episodeModels.isFullSeason(),
                    )
                }
                .filter { item -> item.isReleaseType(type) }

            // Release type applies to episodes only; movies show for All only.
            val movies = if (type != ReleaseType.All) {
                emptyList()
            } else {
                val asyncMap = moviesData
                    .filter {
                        val localDate = LocalDate.parse(it.released)
                            .atStartOfDay(UTC)
                            .toInstant()
                            .toLocalDay()
                        localDate in startDate..endDate
                    }
                    .asyncMap {
                        val id = it.movie.ids.trakt.toTraktId()
                        CalendarItem.MovieItem(
                            watched = moviesProgress.containsKey(id),
                            movie = Movie.fromDto(it.movie),
                        )
                    }
                asyncMap
            }

            // Group by day
            val itemsByDay = (episodes + movies)
                .distinctBy { it.key }
                .filter { it.releasedAt != null }
                .groupBy { it.releasedAt!!.toLocalDay() }

            // Create sorted map with every day in the range, including empty days
            val result = buildMap<LocalDate, ImmutableList<CalendarItem>> {
                for (offset in 0 until days - DAYS_OFFSET) {
                    val currentDay = startDate.plusDays(offset)
                    val items = (itemsByDay[currentDay] ?: emptyList())
                        .sortedBy { it.releasedAt }.toImmutableList()
                    put(currentDay, items)
                }
            }

            result.toImmutableMap()
        }
    }
}

private data class CalendarRequestWindow(
    val startDate: LocalDate,
    val days: Int,
)

// Consecutive windows of at most MAX_REQUEST_DAYS days covering [days] from [startDate].
private fun requestWindows(
    startDate: LocalDate,
    days: Int,
): List<CalendarRequestWindow> {
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
