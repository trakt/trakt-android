package tv.trakt.trakt.core.calendar.usecases

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.persistentMapOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.collections.immutable.toImmutableMap
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import tv.trakt.trakt.common.auth.session.SessionManager
import tv.trakt.trakt.common.core.user.data.remote.calendar.UserCalendarRemoteDataSource
import tv.trakt.trakt.common.core.user.usecases.progress.LoadUserProgressUseCase
import tv.trakt.trakt.common.helpers.extensions.asyncMap
import tv.trakt.trakt.common.helpers.extensions.toInstant
import tv.trakt.trakt.common.helpers.extensions.toLocalDay
import tv.trakt.trakt.common.model.Episode
import tv.trakt.trakt.common.model.Movie
import tv.trakt.trakt.common.model.Show
import tv.trakt.trakt.common.model.fromDto
import tv.trakt.trakt.common.model.toTraktId
import tv.trakt.trakt.core.calendar.model.CalendarItem
import java.time.DayOfWeek.MONDAY
import java.time.DayOfWeek.SUNDAY
import java.time.LocalDate

private const val DAYS_OFFSET = 1L
private const val DAYS_RANGE = 8

// A same-day batch is a full season when it spans more than one episode and
// carries both the season premiere and the season finale.
private fun List<Episode>.isFullSeason(): Boolean {
    if (size <= 1) return false
    val hasPremiere = any { it.type?.isPremiere == true }
    val hasFinale = any { it.type?.isFinale == true }
    return hasPremiere && hasFinale
}

internal class GetCalendarItemsUseCase(
    private val loadUserProgressUseCase: LoadUserProgressUseCase,
    private val remoteUserSource: UserCalendarRemoteDataSource,
    private val sessionManager: SessionManager,
) {
    suspend fun getCalendarItems(day: LocalDate): ImmutableMap<LocalDate, ImmutableList<CalendarItem>> {
        return coroutineScope {
            if (!sessionManager.isAuthenticated()) {
                return@coroutineScope persistentMapOf()
            }

            val (weekStart, weekEnd) = with(day) {
                with(MONDAY) to with(SUNDAY)
            }

            val showsDataAsync = async {
                remoteUserSource.getShowsCalendar(
                    startDate = weekStart.minusDays(DAYS_OFFSET),
                    days = DAYS_RANGE,
                )
            }
            val moviesDataAsync = async {
                remoteUserSource.getMoviesCalendar(
                    startDate = weekStart.minusDays(DAYS_OFFSET),
                    days = DAYS_RANGE,
                )
            }

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

            val showsData = showsDataAsync.await()
            val moviesData = moviesDataAsync.await()

            val showsProgress = showsProgressAsync.await()
                .associateBy { it.showId }
            val moviesProgress = moviesProgressAsync.await()
                .associateBy { it.movie.ids.trakt }

            val weekShowsData = showsData.filter {
                val date = it.episode.effectiveReleaseDate ?: it.firstAired
                val localDate = date.toInstant().toLocalDay()
                it.episode.season > 0 && localDate in weekStart..weekEnd
            }

            val episodes = weekShowsData
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

            val movies = moviesData
                .filter {
                    val localDate = LocalDate.parse(it.released)
                    localDate in weekStart..weekEnd
                }
                .asyncMap {
                    val id = it.movie.ids.trakt.toTraktId()
                    CalendarItem.MovieItem(
                        watched = moviesProgress.containsKey(id),
                        movie = Movie.fromDto(it.movie),
                    )
                }

            // Group by day
            val itemsByDay = (episodes + movies)
                .filter { it.releasedAt != null }
                .groupBy { it.releasedAt!!.toLocalDay() }

            // Create sorted map with all days in the week, including empty days
            val result = buildMap<LocalDate, ImmutableList<CalendarItem>> {
                for (i in 0..6) {
                    val currentDay = weekStart.plusDays(i.toLong())
                    val items = (itemsByDay[currentDay] ?: emptyList())
                        .sortedBy { it.releasedAt }.toImmutableList()
                    put(currentDay, items)
                }
            }

            result.toImmutableMap()
        }
    }
}
