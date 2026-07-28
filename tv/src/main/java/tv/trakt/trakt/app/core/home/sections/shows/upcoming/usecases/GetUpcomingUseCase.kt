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
import tv.trakt.trakt.common.helpers.extensions.toLocal
import tv.trakt.trakt.common.helpers.extensions.toLocalDay
import tv.trakt.trakt.common.model.Episode
import tv.trakt.trakt.common.model.Movie
import tv.trakt.trakt.common.model.Show
import tv.trakt.trakt.common.model.fromDto
import java.time.LocalDate
import java.time.temporal.ChronoUnit.DAYS

private const val DAYS_OFFSET = 1L
private const val DAYS_RANGE = 14

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
                    val episodes = episodeItems.flatMap { item -> item.episodes }
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
        )

        val now = nowLocal().truncatedTo(DAYS)

        return remoteShows
            .mapNotNull {
                val episode = Episode.fromDto(it.episode)
                if (episode.season <= 0) return@mapNotNull null

                val releaseAt = episode.releasedAt?.toLocal() ?: return@mapNotNull null
                if (releaseAt.isBefore(now)) return@mapNotNull null

                Show.fromDto(it.show) to episode
            }
            // Group a show's episodes that share the same release day into one item,
            // so a same-day batch renders as a single card with a combined list.
            .groupBy { (show, episode) -> show.ids.trakt to episode.releasedAt?.toLocalDay() }
            .map { (_, entries) ->
                val episodes = entries
                    .map { (_, episode) -> episode }
                    .sortedBy { it.number }
                    .toImmutableList()

                HomeUpcomingItem.EpisodeItem(
                    show = entries.first().first,
                    episodes = episodes,
                    isFullSeason = episodes.isFullSeason(),
                )
            }
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

// A same-day batch is a full season when it spans more than one episode and
// carries both the season premiere and the season finale.
private fun List<Episode>.isFullSeason(): Boolean {
    if (size <= 1) return false
    val hasPremiere = any { it.type?.isPremiere == true }
    val hasFinale = any { it.type?.isFinale == true }
    return hasPremiere && hasFinale
}
