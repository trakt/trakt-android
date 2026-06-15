package tv.trakt.trakt.core.profile.sections.thismonth.usecases

import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import tv.trakt.trakt.common.helpers.extensions.nowLocal
import tv.trakt.trakt.common.helpers.extensions.toLocal
import tv.trakt.trakt.core.profile.sections.thismonth.model.ProfileStats
import tv.trakt.trakt.core.user.usecases.progress.LoadUserProgressUseCase
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

        val moviesThisMonth = moviesProgress.count {
            it.lastWatchedAt.isInSameMonthAs(currentDate)
        }

        return MoviesCounts(
            moviesThisMonth = moviesThisMonth,
            allMovies = moviesProgress.size,
        )
    }

    private suspend fun loadShowsEpisodesCount(currentDate: ZonedDateTime): ShowsCounts {
        val progress = if (loadUserProgressUseCase.isShowsLoaded()) {
            loadUserProgressUseCase.loadLocalShows()
        } else {
            loadUserProgressUseCase.loadShowsProgress()
        }

        val showsThisMonth = progress.count {
            it.lastWatchedAt.isInSameMonthAs(currentDate)
        }

        val allEpisodes = progress
            .flatMap { it.seasons }
            .flatMap { it.episodes }

        val episodesThisMonth = allEpisodes.count {
            it.lastWatchedAt.isInSameMonthAs(currentDate)
        }

        return ShowsCounts(
            showsThisMonth = showsThisMonth,
            episodesThisMonth = episodesThisMonth,
            allShows = progress.size,
            allEpisodes = allEpisodes.size,
        )
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
