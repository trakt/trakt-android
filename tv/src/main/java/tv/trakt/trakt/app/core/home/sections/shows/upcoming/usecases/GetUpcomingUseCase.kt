package tv.trakt.trakt.app.core.home.sections.shows.upcoming.usecases

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import tv.trakt.trakt.app.core.home.sections.shows.upcoming.model.HomeUpcomingItem
import tv.trakt.trakt.app.core.profile.data.remote.ProfileRemoteDataSource
import tv.trakt.trakt.common.core.episodes.data.local.EpisodeLocalDataSource
import tv.trakt.trakt.common.core.movies.data.local.MovieLocalDataSource
import tv.trakt.trakt.common.core.shows.data.local.ShowLocalDataSource
import tv.trakt.trakt.common.helpers.extensions.asyncMap
import tv.trakt.trakt.common.helpers.extensions.nowLocal
import tv.trakt.trakt.common.helpers.extensions.nowLocalDay
import tv.trakt.trakt.common.helpers.extensions.toInstant
import tv.trakt.trakt.common.helpers.extensions.toLocal
import tv.trakt.trakt.common.model.Episode
import tv.trakt.trakt.common.model.Movie
import tv.trakt.trakt.common.model.Show
import tv.trakt.trakt.common.model.fromDto
import java.time.LocalDate
import java.time.temporal.ChronoUnit.DAYS

private const val DAYS_OFFSET = 1L
private const val DAYS_RANGE = 14

private val premiereValues = listOf("season_premiere", "series_premiere")
private val finaleValues = listOf("season_finale", "series_finale")

internal class GetUpcomingUseCase(
    private val remoteUserSource: ProfileRemoteDataSource,
    private val localShowSource: ShowLocalDataSource,
    private val localMovieSource: MovieLocalDataSource,
    private val localEpisodeSource: EpisodeLocalDataSource,
) {
    suspend fun getUpcoming(): ImmutableList<HomeUpcomingItem> {
        return coroutineScope {
            val showsAsync = async { getShows() }
            val moviesAsync = async { getMovies() }

            return@coroutineScope (
                showsAsync.await() +
                    moviesAsync.await()
            )
                .sortedBy { it.releaseAt }
                .toImmutableList()
                .also {
                    val episodeItems = it.filterIsInstance<HomeUpcomingItem.EpisodeItem>()
                    val movieItems = it.filterIsInstance<HomeUpcomingItem.MovieItem>()

                    val shows = episodeItems.asyncMap { item -> item.show }
                    val episodes = episodeItems.asyncMap { item -> item.episode }
                    val movies = movieItems.asyncMap { item -> item.movie }

                    localShowSource.upsertShows(shows)
                    localMovieSource.upsertMovies(movies)
                    localEpisodeSource.upsertEpisodes(episodes)
                }
        }
    }

    private suspend fun getShows(): List<HomeUpcomingItem.EpisodeItem> {
        val remoteShows = remoteUserSource.getUserShowsCalendar(
            startDate = nowLocalDay().minusDays(DAYS_OFFSET),
            days = DAYS_RANGE,
        ).filter {
            it.episode.season > 0
        }

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

        val now = nowLocal().truncatedTo(DAYS)
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
                    episode = Episode.fromDto(it.episode),
                    show = Show.fromDto(it.show),
                    isFullSeason = isFullSeason,
                )
            }

        return showsList
            .filterNotNull()
    }

    private suspend fun getMovies(): List<HomeUpcomingItem.MovieItem> {
        val remoteMovies = remoteUserSource.getUserMoviesCalendar(
            startDate = nowLocalDay().minusDays(DAYS_OFFSET),
            days = DAYS_RANGE,
        )

        val today = nowLocalDay()
        val moviesList = remoteMovies
            .asyncMap {
                val releaseAt = LocalDate.parse(it.released)
                if (releaseAt.isBefore(today)) {
                    return@asyncMap null
                }

                HomeUpcomingItem.MovieItem(
                    movie = Movie.fromDto(it.movie),
                )
            }

        return moviesList
            .filterNotNull()
    }
}
