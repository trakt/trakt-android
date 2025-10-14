package tv.trakt.trakt.core.home.sections.upcoming.usecases

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import tv.trakt.trakt.common.helpers.extensions.asyncMap
import tv.trakt.trakt.common.helpers.extensions.nowLocal
import tv.trakt.trakt.common.helpers.extensions.nowLocalDay
import tv.trakt.trakt.common.helpers.extensions.toInstant
import tv.trakt.trakt.common.model.Episode
import tv.trakt.trakt.common.model.Movie
import tv.trakt.trakt.common.model.Show
import tv.trakt.trakt.common.model.fromDto
import tv.trakt.trakt.common.model.toTraktId
import tv.trakt.trakt.core.home.HomeConfig.HOME_UPCOMING_DAYS_LIMIT
import tv.trakt.trakt.core.home.sections.upcoming.data.local.HomeUpcomingLocalDataSource
import tv.trakt.trakt.core.home.sections.upcoming.model.HomeUpcomingItem
import tv.trakt.trakt.core.user.data.remote.UserRemoteDataSource
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

private val premiereValues = listOf("season_premiere", "series_premiere")
private val finaleValues = listOf("season_finale", "series_finale")

internal class GetUpcomingUseCase(
    private val remoteUserSource: UserRemoteDataSource,
    private val localDataSource: HomeUpcomingLocalDataSource,
) {
    suspend fun getLocalUpcoming(): ImmutableList<HomeUpcomingItem> {
        return localDataSource.getItems()
            .sortedBy { it.releasedAt }
            .toImmutableList()
    }

    suspend fun getUpcoming(): ImmutableList<HomeUpcomingItem> {
        return coroutineScope {
            val showsAsync = async { getShows() }
            val moviesAsync = async { getMovies() }

            return@coroutineScope (
                showsAsync.await() +
                    moviesAsync.await()
            )
                .sortedBy { it.releasedAt }
                .toImmutableList()
                .also {
                    localDataSource.addItems(
                        items = it,
                    )
                }
        }
    }

    private suspend fun getShows(): List<HomeUpcomingItem.EpisodeItem> {
        val remoteShows = remoteUserSource.getShowsCalendar(
            startDate = nowLocalDay(),
            days = HOME_UPCOMING_DAYS_LIMIT,
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

        val showsList = remoteShows
            .asyncMap {
                val releaseAt = it.firstAired.toInstant()
                if (releaseAt.isBefore(Instant.now())) {
                    return@asyncMap null
                }

                val isFullSeason = fullSeasonItems[it.show.ids.trakt] != null
                if (isFullSeason && it.episode.number > 1) {
                    return@asyncMap null
                }

                HomeUpcomingItem.EpisodeItem(
                    id = it.episode.ids.trakt.toTraktId(),
                    releasedAt = releaseAt,
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
            startDate = nowLocal().toLocalDate(),
            days = HOME_UPCOMING_DAYS_LIMIT,
        )

        val moviesList = remoteMovies
            .asyncMap {
                val releaseAt = LocalDate.parse(it.released)
                    .atStartOfDay(ZoneId.of("UTC"))
                    .toInstant()

                if (releaseAt.isBefore(Instant.now())) {
                    return@asyncMap null
                }

                HomeUpcomingItem.MovieItem(
                    id = it.movie.ids.trakt.toTraktId(),
                    releasedAt = releaseAt,
                    movie = Movie.fromDto(it.movie),
                )
            }

        return moviesList
            .filterNotNull()
    }
}
