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
import tv.trakt.trakt.common.model.Episode
import tv.trakt.trakt.common.model.Movie
import tv.trakt.trakt.common.model.Show
import tv.trakt.trakt.common.model.fromDto
import tv.trakt.trakt.common.model.toTraktId
import tv.trakt.trakt.core.home.sections.upcoming.data.local.HomeUpcomingLocalDataSource
import tv.trakt.trakt.core.home.sections.upcoming.model.HomeUpcomingItem
import tv.trakt.trakt.core.main.model.MediaMode
import tv.trakt.trakt.core.main.model.MediaMode.MEDIA
import tv.trakt.trakt.core.main.model.MediaMode.MOVIES
import tv.trakt.trakt.core.main.model.MediaMode.SHOWS
import java.time.LocalDate
import java.time.ZoneId

private const val DAYS_OFFSET = 1L
private const val DAYS_RANGE = 14

private val premiereValues = listOf("season_premiere", "series_premiere")
private val finaleValues = listOf("season_finale", "series_finale")

internal class GetUpcomingUseCase(
    private val remoteUserSource: UserCalendarRemoteDataSource,
    private val localDataSource: HomeUpcomingLocalDataSource,
) {
    suspend fun getLocalUpcoming(filter: MediaMode): ImmutableList<HomeUpcomingItem> {
        return localDataSource.getItems()
            .filter {
                when (filter) {
                    MEDIA -> true
                    SHOWS -> it is HomeUpcomingItem.EpisodeItem
                    MOVIES -> it is HomeUpcomingItem.MovieItem
                }
            }
            .sortedBy { it.releasedAt }
            .toImmutableList()
    }

    suspend fun getUpcoming(filter: MediaMode): ImmutableList<HomeUpcomingItem> {
        return coroutineScope {
            val showsAsync = async { getShows() }
            val moviesAsync = async { getMovies() }

            return@coroutineScope (
                showsAsync.await() +
                    moviesAsync.await()
            )
                .sortedBy { it.releasedAt }
                .also {
                    localDataSource.setItems(
                        items = it,
                    )
                }
                .filter {
                    when (filter) {
                        MEDIA -> true
                        SHOWS -> it is HomeUpcomingItem.EpisodeItem
                        MOVIES -> it is HomeUpcomingItem.MovieItem
                    }
                }
                .toImmutableList()
        }
    }

    private suspend fun getShows(): List<HomeUpcomingItem.EpisodeItem> {
        val remoteShows = remoteUserSource.getShowsCalendar(
            startDate = nowLocalDay().minusDays(DAYS_OFFSET),
            days = DAYS_RANGE,
        )

        val fullSeasonItems = remoteShows
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

        val now = nowLocal()
        val showsList = remoteShows
            .asyncMap {
                val releaseAt = (it.episode.effectiveReleaseDate ?: it.episode.firstAired)
                    ?.toInstant()
                    ?.toLocal()

                if (releaseAt == null) {
                    return@asyncMap null
                }

                if (releaseAt.isBefore(now)) {
                    return@asyncMap null
                }

                val isFullSeason = fullSeasonItems[it.show.ids.trakt] != null
                if (isFullSeason && it.episode.number > 1) {
                    return@asyncMap null
                }

                HomeUpcomingItem.EpisodeItem(
                    id = it.episode.ids.trakt.toTraktId(),
                    releasedAt = releaseAt.toInstant(),
                    episode = Episode.fromDto(it.episode),
                    show = Show.fromDto(it.show),
                    isFullSeason = isFullSeason,
                )
            }

        return showsList
            .filterNotNull()
    }

    private suspend fun getMovies(): List<HomeUpcomingItem.MovieItem> {
        val remoteMovies = remoteUserSource.getMoviesCalendar(
            startDate = nowLocalDay().minusDays(DAYS_OFFSET),
            days = DAYS_RANGE,
        )

        val now = nowLocalDay()
        val moviesList = remoteMovies
            .asyncMap {
                val releaseAt = LocalDate.parse(it.released)

                if (releaseAt.isBefore(now)) {
                    return@asyncMap null
                }

                HomeUpcomingItem.MovieItem(
                    id = it.movie.ids.trakt.toTraktId(),
                    releasedAt = releaseAt.atStartOfDay(ZoneId.systemDefault()).toInstant(),
                    movie = Movie.fromDto(it.movie),
                )
            }

        return moviesList
            .filterNotNull()
    }
}
