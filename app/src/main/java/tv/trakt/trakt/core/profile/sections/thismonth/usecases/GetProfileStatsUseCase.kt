package tv.trakt.trakt.core.profile.sections.thismonth.usecases

import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import tv.trakt.trakt.common.helpers.extensions.nowLocal
import tv.trakt.trakt.common.helpers.extensions.toLocal
import tv.trakt.trakt.core.profile.sections.thismonth.model.ProfileStats
import tv.trakt.trakt.core.user.usecases.progress.LoadUserProgressUseCase
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
        val moviesProgress = when {
            !loadUserProgressUseCase.isMoviesLoaded() -> {
                loadUserProgressUseCase.loadMoviesProgress()
            }

            else -> {
                loadUserProgressUseCase.loadLocalMovies()
            }
        }

        val moviesThisMonth = moviesProgress.count {
            val localWatchedAt = it.lastWatchedAt.toLocal()
            localWatchedAt.year == currentDate.year &&
                localWatchedAt.month == currentDate.month
        }

        return MoviesCounts(
            moviesThisMonth = moviesThisMonth,
            allMovies = moviesProgress.size,
        )
    }

    private suspend fun loadShowsEpisodesCount(currentDate: ZonedDateTime): ShowsCounts {
        val progress = when {
            !loadUserProgressUseCase.isShowsLoaded() -> {
                loadUserProgressUseCase.loadShowsProgress()
            }

            else -> {
                loadUserProgressUseCase.loadLocalShows()
            }
        }

        val showsThisMonth = progress.count {
            val localWatchedAt = it.lastWatchedAt.toLocal()
            localWatchedAt.year == currentDate.year &&
                localWatchedAt.month == currentDate.month
        }

        val allEpisodes = progress
            .flatMap { it.seasons }
            .flatMap { it.episodes }

        val episodesThisMonth = allEpisodes.count {
            val localWatchedAt = it.lastWatchedAt.toLocal()
            localWatchedAt.year == currentDate.year &&
                localWatchedAt.month == currentDate.month
        }

        return ShowsCounts(
            showsThisMonth = showsThisMonth,
            episodesThisMonth = episodesThisMonth,
            allShows = progress.size,
            allEpisodes = allEpisodes.size,
        )
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
