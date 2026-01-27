package tv.trakt.trakt.core.calendar.usecases

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.toImmutableList
import kotlinx.collections.immutable.toImmutableMap
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import tv.trakt.trakt.common.helpers.extensions.asyncMap
import tv.trakt.trakt.common.helpers.extensions.toInstant
import tv.trakt.trakt.common.helpers.extensions.toLocal
import tv.trakt.trakt.common.helpers.extensions.toLocalDay
import tv.trakt.trakt.common.model.Episode
import tv.trakt.trakt.common.model.Movie
import tv.trakt.trakt.common.model.Show
import tv.trakt.trakt.common.model.fromDto
import tv.trakt.trakt.common.model.toTraktId
import tv.trakt.trakt.core.home.sections.upcoming.model.HomeUpcomingItem
import tv.trakt.trakt.core.user.data.remote.UserRemoteDataSource
import java.time.DayOfWeek.MONDAY
import java.time.DayOfWeek.SUNDAY
import java.time.LocalDate
import java.time.ZoneId

private const val DAYS_OFFSET = 1L
private const val DAYS_RANGE = 8

private val premiereValues = listOf("season_premiere", "series_premiere")
private val finaleValues = listOf("season_finale", "series_finale")

internal class GetCalendarItemsUseCase(
    private val remoteUserSource: UserRemoteDataSource,
) {
    suspend fun getCalendarItems(day: LocalDate): ImmutableMap<LocalDate, ImmutableList<HomeUpcomingItem>> {
        return coroutineScope {
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

            val showsData = showsDataAsync.await()
            val moviesData = moviesDataAsync.await()

            val fullSeasonItems = showsData
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

            val episodes = showsData
                .filter {
                    val localDate = it.firstAired.toInstant().toLocal().toLocalDate()
                    localDate in weekStart..weekEnd
                }
                .asyncMap {
                    val isFullSeason = fullSeasonItems[it.show.ids.trakt] != null
                    if (isFullSeason && it.episode.number > 1) {
                        return@asyncMap null
                    }

                    HomeUpcomingItem.EpisodeItem(
                        id = it.episode.ids.trakt.toTraktId(),
                        releasedAt = it.firstAired.toInstant(),
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
                    val releaseAt = LocalDate.parse(it.released)
                        .atStartOfDay(ZoneId.of("UTC"))
                        .toInstant()

                    HomeUpcomingItem.MovieItem(
                        id = it.movie.ids.trakt.toTraktId(),
                        releasedAt = releaseAt,
                        movie = Movie.fromDto(it.movie),
                    )
                }

            // Group by day
            val groupedItems = (episodes + movies)
                .sortedBy { it.releasedAt }
                .groupBy { it.releasedAt.toLocalDay() }
                .mapValues { it.value.toImmutableList() }
                .toMutableMap()

            // Iterate over selected week and fill grouped items with empty lists if no items for that day
            for (i in 0..6) {
                val currentDay = weekStart.plusDays(i.toLong())

                if (groupedItems[currentDay] == null) {
                    groupedItems[currentDay] = emptyList<HomeUpcomingItem>().toImmutableList()
                }
            }

            groupedItems
                .toSortedMap()
                .toImmutableMap()
        }
    }
}
