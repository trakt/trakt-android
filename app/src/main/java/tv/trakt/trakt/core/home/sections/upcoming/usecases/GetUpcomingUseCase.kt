package tv.trakt.trakt.core.home.sections.upcoming.usecases

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import tv.trakt.trakt.common.core.user.data.remote.calendar.UserCalendarRemoteDataSource
import tv.trakt.trakt.common.helpers.extensions.asyncMap
import tv.trakt.trakt.common.helpers.extensions.nowLocal
import tv.trakt.trakt.common.helpers.extensions.nowLocalDay
import tv.trakt.trakt.common.helpers.extensions.toInstant
import tv.trakt.trakt.common.helpers.extensions.toLocal
import tv.trakt.trakt.common.helpers.extensions.toLocalDay
import tv.trakt.trakt.common.model.Episode
import tv.trakt.trakt.common.model.MediaMode.Media
import tv.trakt.trakt.common.model.MediaMode.Movies
import tv.trakt.trakt.common.model.MediaMode.Shows
import tv.trakt.trakt.common.model.Movie
import tv.trakt.trakt.common.model.Show
import tv.trakt.trakt.common.model.fromDto
import tv.trakt.trakt.common.model.globalfilter.GlobalFilter
import tv.trakt.trakt.core.calendar.model.CalendarItem
import tv.trakt.trakt.core.discover.sections.releases.model.ReleaseType
import tv.trakt.trakt.core.home.sections.upcoming.data.local.HomeUpcomingLocalDataSource
import java.time.LocalDate
import java.time.temporal.ChronoUnit.DAYS

private const val DAYS_OFFSET = 1L
private const val DAYS_RANGE = 30

// A same-day batch is a full season when it spans more than one episode and
// carries both the season premiere and the season finale.
private fun List<Episode>.isFullSeason(): Boolean {
    if (size <= 1) return false
    val hasPremiere = any { it.type?.isPremiere == true }
    val hasFinale = any { it.type?.isFinale == true }
    return hasPremiere && hasFinale
}

internal class GetUpcomingUseCase(
    private val remoteUserSource: UserCalendarRemoteDataSource,
    private val localDataSource: HomeUpcomingLocalDataSource,
) {
    suspend fun getLocalUpcoming(
        filter: GlobalFilter,
        type: ReleaseType,
    ): ImmutableList<CalendarItem> {
        return localDataSource.getItems()
            .filter {
                when (filter.mode) {
                    Media -> true
                    Shows -> it is CalendarItem.EpisodeItem
                    Movies -> it is CalendarItem.MovieItem
                }
            }
            .filterByType(type)
            .sortedBy { it.releasedAt }
            .toImmutableList()
    }

    suspend fun getUpcoming(
        filter: GlobalFilter,
        type: ReleaseType,
    ): ImmutableList<CalendarItem> {
        return coroutineScope {
            val showsAsync = async { getShows(filter) }
            val moviesAsync = async { getMovies(filter) }
            val items = showsAsync.await() + moviesAsync.await()

            return@coroutineScope items
                .sortedBy { it.releasedAt }
                .also {
                    localDataSource.setItems(
                        items = it,
                    )
                }
                .filter {
                    when (filter.mode) {
                        Media -> true
                        Shows -> it is CalendarItem.EpisodeItem
                        Movies -> it is CalendarItem.MovieItem
                    }
                }
                .filterByType(type)
                .toImmutableList()
        }
    }

    private suspend fun getShows(filter: GlobalFilter): List<CalendarItem.EpisodeItem> {
        val remoteShows = remoteUserSource.getShowsCalendar(
            startDate = nowLocalDay().minusDays(DAYS_OFFSET),
            days = DAYS_RANGE,
            filters = filter,
        ).filter {
            it.episode.season > 0
        }

        val now = nowLocal().truncatedTo(DAYS)

        return remoteShows
            .mapNotNull {
                val releaseAt = (it.episode.effectiveReleaseDate ?: it.episode.firstAired)
                    ?.toInstant()
                    ?.toLocal()
                    ?: return@mapNotNull null

                if (releaseAt.isBefore(now)) {
                    return@mapNotNull null
                }

                Triple(it.show, Episode.fromDto(it.episode), releaseAt.toInstant())
            }
            // Group a show's episodes that share the same release day into one item,
            // so a same-day batch renders as a single card with a combined list.
            .groupBy { (show, _, releasedAt) -> show.ids.trakt to releasedAt.toLocalDay() }
            .map { (_, entries) ->
                val sorted = entries.sortedBy { (_, episode, _) -> episode.number }
                val show = Show.fromDto(sorted.first().first)
                val episodes = sorted
                    .map { (_, episode, _) -> episode }
                    .toImmutableList()

                CalendarItem.EpisodeItem(
                    show = show,
                    episodes = episodes,
                    isFullSeason = episodes.isFullSeason(),
                    watched = false,
                )
            }
    }

    private suspend fun getMovies(filter: GlobalFilter): List<CalendarItem.MovieItem> {
        val remoteMovies = remoteUserSource.getMoviesCalendar(
            startDate = nowLocalDay().minusDays(DAYS_OFFSET),
            days = DAYS_RANGE,
            filters = filter,
        )

        val today = nowLocalDay()
        val moviesList = remoteMovies
            .asyncMap {
                val releaseDay = LocalDate.parse(it.released)
                if (releaseDay.isBefore(today)) {
                    return@asyncMap null
                }

                CalendarItem.MovieItem(
                    movie = Movie.fromDto(it.movie),
                    watched = false,
                )
            }

        return moviesList
            .filterNotNull()
    }

    // Release type applies to episodes only; Premiere/Finale keep grouped episode
    // items containing a matching episode and drop movies entirely.
    private fun List<CalendarItem>.filterByType(type: ReleaseType): List<CalendarItem> {
        if (type == ReleaseType.All) {
            return this
        }
        return filter { item ->
            item is CalendarItem.EpisodeItem &&
                item.episodes.any { episode ->
                    when (type) {
                        ReleaseType.Premiere -> episode.type?.isPremiere == true
                        ReleaseType.Finale -> episode.type?.isFinale == true
                    }
                }
        }
    }
}
