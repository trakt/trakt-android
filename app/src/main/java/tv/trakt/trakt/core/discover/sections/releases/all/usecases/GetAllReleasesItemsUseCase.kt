package tv.trakt.trakt.core.discover.sections.releases.all.usecases

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.toImmutableList
import kotlinx.collections.immutable.toImmutableMap
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import tv.trakt.trakt.common.auth.session.SessionManager
import tv.trakt.trakt.common.core.user.usecases.progress.LoadUserProgressUseCase
import tv.trakt.trakt.common.helpers.extensions.toLocalDay
import tv.trakt.trakt.common.model.globalfilter.GlobalFilter
import tv.trakt.trakt.core.calendar.model.CalendarItem
import tv.trakt.trakt.core.discover.sections.releases.usecases.movies.GetReleasesMoviesUseCase
import tv.trakt.trakt.core.discover.sections.releases.usecases.shows.GetReleasesShowsUseCase
import java.time.DayOfWeek.MONDAY
import java.time.LocalDate
import java.time.ZoneOffset.UTC

private const val WEEK_DAYS = 7
private const val FETCH_PADDING_DAYS = 1
private const val FETCH_DAYS = WEEK_DAYS + FETCH_PADDING_DAYS * 2

internal class GetAllReleasesItemsUseCase(
    private val getReleasesShowsUseCase: GetReleasesShowsUseCase,
    private val getReleasesMoviesUseCase: GetReleasesMoviesUseCase,
    private val loadUserProgressUseCase: LoadUserProgressUseCase,
    private val sessionManager: SessionManager,
) {
    suspend fun getReleaseItems(
        startDay: LocalDate,
        filters: GlobalFilter,
    ): ImmutableMap<LocalDate, ImmutableList<CalendarItem>> {
        val weekStart = startDay.with(MONDAY)
        val rawItems = loadItems(weekStart, filters)

        val itemsByDay = markWatched(rawItems)
            .filter { it.releasedAt != null }
            .groupBy { it.releasedAt!!.toLocalDay() }

        return buildMap<LocalDate, ImmutableList<CalendarItem>> {
            for (i in 0..6) {
                val currentDay = weekStart.plusDays(i.toLong())
                val items = (itemsByDay[currentDay] ?: emptyList())
                    .sortedBy { it.releasedAt }
                    .toImmutableList()
                put(currentDay, items)
            }
        }.toImmutableMap()
    }

    private suspend fun loadItems(
        weekStart: LocalDate,
        filters: GlobalFilter,
    ): List<CalendarItem> {
        return coroutineScope {
            // Backend works in UTC dates; fetch a UTC window padded by one day on
            // each side so items near the UTC/local day boundary aren't lost. The
            // caller groups by local day and only keeps the weekStart..+6 buckets.
            val startDate = weekStart.minusDays(FETCH_PADDING_DAYS.toLong())
                .atStartOfDay(UTC)
                .toInstant()

            val showsAsync = async {
                getReleasesShowsUseCase.getShows(
                    startDate = startDate,
                    days = FETCH_DAYS,
                    filters = filters,
                    skipLocal = true,
                )
            }

            val moviesAsync = async {
                getReleasesMoviesUseCase.getMovies(
                    startDate = startDate,
                    days = FETCH_DAYS,
                    filters = filters,
                    skipLocal = true,
                )
            }

            val shows = if (filters.mode.isMediaOrShows) showsAsync.await() else emptyList()
            val movies = if (filters.mode.isMediaOrMovies) moviesAsync.await() else emptyList()

            shows + movies
        }
    }

    private suspend fun markWatched(items: List<CalendarItem>): List<CalendarItem> {
        if (!sessionManager.isAuthenticated()) {
            return items
        }

        return coroutineScope {
            val showsProgressAsync = async {
                with(loadUserProgressUseCase) {
                    when {
                        isShowsLoaded() -> loadLocalShows()
                        else -> loadShowsProgress()
                    }
                }
            }
            val moviesProgressAsync = async {
                with(loadUserProgressUseCase) {
                    when {
                        isMoviesLoaded() -> loadLocalMovies()
                        else -> loadMoviesProgress()
                    }
                }
            }

            val showsProgress = showsProgressAsync.await()
                .associateBy { it.showId }
            val moviesProgress = moviesProgressAsync.await()
                .associateBy { it.movie.ids.trakt }

            items.map { item ->
                when (item) {
                    is CalendarItem.EpisodeItem -> item.copy(
                        watched = showsProgress[item.show.ids.trakt]?.isEpisodeWatched(
                            seasonNumber = item.episode.season,
                            episodeId = item.episode.ids.trakt,
                        ) == true,
                    )

                    is CalendarItem.MovieItem -> item.copy(
                        watched = moviesProgress.containsKey(item.movie.ids.trakt),
                    )
                }
            }
        }
    }
}
