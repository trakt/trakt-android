package tv.trakt.trakt.core.calendar.usecases

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.persistentMapOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.collections.immutable.toImmutableMap
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import tv.trakt.trakt.common.auth.session.SessionManager
import tv.trakt.trakt.common.helpers.extensions.asyncMap
import tv.trakt.trakt.common.helpers.extensions.toInstant
import tv.trakt.trakt.common.helpers.extensions.toLocal
import tv.trakt.trakt.common.helpers.extensions.toLocalDay
import tv.trakt.trakt.common.model.Episode
import tv.trakt.trakt.common.model.Movie
import tv.trakt.trakt.common.model.Show
import tv.trakt.trakt.common.model.fromDto
import tv.trakt.trakt.common.model.toTraktId
import tv.trakt.trakt.core.calendar.model.CalendarItem
import tv.trakt.trakt.core.user.data.remote.UserRemoteDataSource
import tv.trakt.trakt.core.user.usecases.progress.LoadUserProgressUseCase
import java.time.DayOfWeek.MONDAY
import java.time.DayOfWeek.SUNDAY
import java.time.LocalDate

private const val DAYS_OFFSET = 1L
private const val DAYS_RANGE = 8

private val premiereValues = listOf("season_premiere", "series_premiere")
private val finaleValues = listOf("season_finale", "series_finale")

internal class GetCalendarItemsUseCase(
    private val loadUserProgressUseCase: LoadUserProgressUseCase,
    private val remoteUserSource: UserRemoteDataSource,
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
                .associateBy { it.show.ids.trakt }
            val moviesProgress = moviesProgressAsync.await()
                .associateBy { it.movie.ids.trakt }

            val weekShowsData = showsData.filter {
                val localDate = it.firstAired.toInstant().toLocal().toLocalDate()
                localDate in weekStart..weekEnd
            }

            val fullSeasonItems = weekShowsData
                .groupBy { it.show.ids.trakt }
                .filter { (_, episodes) ->
                    val isSeasonPremiere = episodes.any {
                        it.episode.episodeType?.value in premiereValues
                    }

                    val isSeasonFinale = episodes.any {
                        it.episode.episodeType?.value in finaleValues
                    }

                    return@filter episodes.size > 1 && isSeasonPremiere && isSeasonFinale
                }

            val episodes = weekShowsData
                .asyncMap {
                    val isFullSeason = fullSeasonItems[it.show.ids.trakt] != null
                    if (isFullSeason && it.episode.number > 1) {
                        return@asyncMap null
                    }

                    val showId = it.show.ids.trakt.toTraktId()
                    CalendarItem.EpisodeItem(
                        watched = showsProgress[showId]?.isEpisodeWatched(
                            season = it.episode.season,
                            episode = it.episode.number,
                        ) == true,
                        episode = Episode.fromDto(it.episode),
                        show = Show.fromDto(it.show),
                        isFullSeason = isFullSeason,
                    )
                }
                .filterNotNull()

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
