package tv.trakt.trakt.core.profile.sections.thismonth.usecases

import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
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

        val moviePlays = moviesProgress
            .asSequence()
            .flatMap { it.plays }

        return MoviesCounts(
            moviesThisMonth = moviePlays.count {
                it.isInSameMonthAs(currentDate)
            },
            allMovies = moviePlays.count(),
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

        val episodePlays = progress
            .asSequence()
            .flatMap { it.seasons }
            .flatMap { it.episodes }
            .flatMap { it.plays }

        val allEpisodes = episodePlays.count()
        val episodesThisMonth = episodePlays.count { it.isInSameMonthAs(currentDate) }

        return ShowsCounts(
            showsThisMonth = showsThisMonth,
            episodesThisMonth = episodesThisMonth,
            allShows = progress.size,
            allEpisodes = allEpisodes,
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
