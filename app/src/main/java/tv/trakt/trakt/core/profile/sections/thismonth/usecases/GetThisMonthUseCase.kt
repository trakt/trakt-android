package tv.trakt.trakt.core.profile.sections.thismonth.usecases

import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import tv.trakt.trakt.common.helpers.extensions.nowLocal
import tv.trakt.trakt.common.helpers.extensions.toLocal
import tv.trakt.trakt.core.profile.sections.thismonth.model.ThisMonthStats
import tv.trakt.trakt.core.user.usecases.progress.LoadUserProgressUseCase
import java.time.ZonedDateTime

internal class GetThisMonthUseCase(
    private val loadUserProgressUseCase: LoadUserProgressUseCase,
) {
    suspend fun getThisMonthStats(): ThisMonthStats {
        return coroutineScope {
            val currentDate = nowLocal()

            val episodesCountAsync = async { loadShowsEpisodesCount(currentDate) }
            val moviesCountAsync = async { loadMoviesCount(currentDate) }

            val (showsCount, episodesCount) = episodesCountAsync.await()
            val moviesCount = moviesCountAsync.await()

            ThisMonthStats(
                showsCount = showsCount,
                episodesCount = episodesCount,
                moviesCount = moviesCount,
            )
        }
    }

    private suspend fun loadMoviesCount(currentDate: ZonedDateTime): Int {
        val moviesProgress = when {
            !loadUserProgressUseCase.isMoviesLoaded() -> {
                loadUserProgressUseCase.loadMoviesProgress()
            }

            else -> {
                loadUserProgressUseCase.loadLocalMovies()
            }
        }

        return moviesProgress.count {
            val localWatchedAt = it.lastWatchedAt.toLocal()
            localWatchedAt.year == currentDate.year &&
                localWatchedAt.month == currentDate.month
        }
    }

    private suspend fun loadShowsEpisodesCount(currentDate: ZonedDateTime): Pair<Int, Int> {
        val progress = when {
            !loadUserProgressUseCase.isShowsLoaded() -> {
                loadUserProgressUseCase.loadShowsProgress()
            }

            else -> {
                loadUserProgressUseCase.loadLocalShows()
            }
        }

        val showsCount = progress.count {
            val localWatchedAt = it.lastWatchedAt.toLocal()
            localWatchedAt.year == currentDate.year &&
                localWatchedAt.month == currentDate.month
        }

        val episodesCount = progress
            .flatMap { it.seasons }
            .flatMap { it.episodes }
            .count {
                val localWatchedAt = it.lastWatchedAt.toLocal()
                localWatchedAt.year == currentDate.year &&
                    localWatchedAt.month == currentDate.month
            }

        return showsCount to episodesCount
    }
}
