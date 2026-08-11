package tv.trakt.trakt.core.profile.sections.thismonth.usecases

import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import tv.trakt.trakt.common.core.sync.model.ProgressItem
import tv.trakt.trakt.common.core.user.usecases.progress.LoadUserProgressUseCase
import tv.trakt.trakt.common.helpers.extensions.nowLocal
import tv.trakt.trakt.common.helpers.extensions.toLocal
import tv.trakt.trakt.core.profile.sections.thismonth.model.ProfileStats
import java.time.Instant
import java.time.ZonedDateTime

internal class GetProfileStatsUseCase(
    private val loadUserProgressUseCase: LoadUserProgressUseCase,
) {
    suspend fun getProfileStats(): ProfileStats {
        return coroutineScope {
            val currentDate = nowLocal()

            val showsAsync = async { loadShowsEpisodesCount(currentDate) }
            val moviesAsync = async { loadMoviesCount(currentDate) }

            val shows = showsAsync.await()
            val movies = moviesAsync.await()

            ProfileStats(
                showsCount = shows.showsThisMonth,
                episodesCount = shows.episodesThisMonth,
                moviesCount = movies.moviesThisMonth,
                allShowsCount = shows.allShows,
                allEpisodesCount = shows.allEpisodes,
                allMoviesCount = movies.allMovies,
            )
        }
    }

    private suspend fun loadMoviesCount(currentDate: ZonedDateTime): MoviesCounts {
        val moviesProgress = if (loadUserProgressUseCase.isMoviesLoaded()) {
            loadUserProgressUseCase.loadLocalMovies()
        } else {
            loadUserProgressUseCase.loadMoviesProgress()
        }

        val watchedMovies = moviesProgress
            .filter { it.plays.isNotEmpty() }
            .distinctBy { it.movie.ids.trakt }

        return MoviesCounts(
            moviesThisMonth = watchedMovies.count { movie ->
                movie.plays.any { it.isInSameMonthAs(currentDate) }
            },
            allMovies = watchedMovies.size,
        )
    }

    private suspend fun loadShowsEpisodesCount(currentDate: ZonedDateTime): ShowsCounts {
        val progress = if (loadUserProgressUseCase.isShowsLoaded()) {
            loadUserProgressUseCase.loadLocalShows()
        } else {
            loadUserProgressUseCase.loadShowsProgress()
        }

        val watchedShows = progress
            .distinctBy { it.showId }
            .map { show ->
                show.seasons
                    .flatMap { it.episodes }
                    .filter { it.plays.isNotEmpty() }
                    .distinctBy { it.id }
            }
            .filter { it.isNotEmpty() }

        val watchedEpisodes = watchedShows.flatten()

        return ShowsCounts(
            showsThisMonth = watchedShows.count { episodes ->
                episodes.any { it.wasWatchedIn(currentDate) }
            },
            episodesThisMonth = watchedEpisodes.count { it.wasWatchedIn(currentDate) },
            allShows = watchedShows.size,
            allEpisodes = watchedEpisodes.size,
        )
    }

    private fun ProgressItem.ShowItem.Episode.wasWatchedIn(reference: ZonedDateTime): Boolean {
        return plays.any { it.isInSameMonthAs(reference) }
    }

    private fun Instant.isInSameMonthAs(reference: ZonedDateTime): Boolean {
        val local = toLocal()
        return local.year == reference.year && local.month == reference.month
    }

    private data class MoviesCounts(
        val moviesThisMonth: Int,
        val allMovies: Int,
    )

    private data class ShowsCounts(
        val showsThisMonth: Int,
        val episodesThisMonth: Int,
        val allShows: Int,
        val allEpisodes: Int,
    )
}
