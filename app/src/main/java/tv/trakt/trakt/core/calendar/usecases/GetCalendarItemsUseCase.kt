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
import tv.trakt.trakt.common.model.Episode
import tv.trakt.trakt.common.model.Movie
import tv.trakt.trakt.common.model.Show
import tv.trakt.trakt.common.model.fromDto
import tv.trakt.trakt.common.model.toTraktId
import tv.trakt.trakt.core.home.sections.upcoming.model.HomeUpcomingItem
import tv.trakt.trakt.core.user.data.remote.UserRemoteDataSource
import java.time.DayOfWeek.MONDAY
import java.time.DayOfWeek.SUNDAY
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.ChronoUnit

private const val DAYS_OFFSET = 1L
private const val DAYS_RANGE = 8

internal class GetCalendarItemsUseCase(
    private val remoteUserSource: UserRemoteDataSource,
) {
    suspend fun getCalendarItems(day: LocalDate): ImmutableMap<Instant, ImmutableList<HomeUpcomingItem>> {
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

            val episodes = showsData
                .filter {
                    val localDate = it.firstAired.toInstant().toLocal().toLocalDate()
                    localDate in weekStart..weekEnd
                }
                .asyncMap {
                    HomeUpcomingItem.EpisodeItem(
                        id = it.episode.ids.trakt.toTraktId(),
                        releasedAt = it.firstAired.toInstant(),
                        episode = Episode.fromDto(it.episode),
                        show = Show.fromDto(it.show),
                    )
                }

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
                .groupBy {
                    it.releasedAt
                        .toLocal()
                        .truncatedTo(ChronoUnit.DAYS)
                        .toInstant()
                }
                .mapValues { it.value.toImmutableList() }
                .toImmutableMap()

            groupedItems
        }
    }
}
