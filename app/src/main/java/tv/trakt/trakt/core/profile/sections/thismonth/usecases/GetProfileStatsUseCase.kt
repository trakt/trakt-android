package tv.trakt.trakt.core.profile.sections.thismonth.usecases

import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import timber.log.Timber
import tv.trakt.trakt.common.auth.session.SessionManager
import tv.trakt.trakt.common.core.sync.model.ProgressItem
import tv.trakt.trakt.common.core.user.usecases.progress.LoadUserProgressUseCase
import tv.trakt.trakt.common.core.user.usecases.ratings.LoadUserRatingsUseCase
import tv.trakt.trakt.common.data.remote.UserRemoteSource
import tv.trakt.trakt.common.helpers.extensions.nowLocal
import tv.trakt.trakt.common.helpers.extensions.recordError
import tv.trakt.trakt.common.helpers.extensions.rethrowCancellation
import tv.trakt.trakt.common.helpers.extensions.toLocal
import tv.trakt.trakt.common.model.UserStats
import tv.trakt.trakt.core.profile.sections.thismonth.model.AllTimeStats
import tv.trakt.trakt.core.profile.sections.thismonth.model.ProfileStats
import java.time.Instant
import java.time.ZonedDateTime

internal class GetProfileStatsUseCase(
    private val sessionManager: SessionManager,
    private val loadUserProgressUseCase: LoadUserProgressUseCase,
    private val loadUserRatingsUseCase: LoadUserRatingsUseCase,
    private val userRemoteSource: UserRemoteSource,
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
                episodesCount = shows.playsThisMonth,
                moviesCount = movies.playsThisMonth,
                playsCount = shows.playsThisMonth + movies.playsThisMonth,
                allShowsCount = shows.allShows,
                allEpisodesCount = shows.allEpisodes,
                allMoviesCount = movies.allMovies,
                allPlaysCount = shows.allPlays + movies.allPlays,
            )
        }
    }

    suspend fun getAllTimeStats(): AllTimeStats {
        if (sessionManager.getProfile()?.isAnyVip == true) {
            return AllTimeStats(
                remote = userRemoteSource.getStats(userId = "me")?.let(UserStats::fromDto),
            )
        }

        return coroutineScope {
            val ratingsAsync = async { loadRatingsCount() }
            val commentsAsync = async { loadCommentsCount() }

            AllTimeStats(
                ratingsCount = ratingsAsync.await(),
                commentsCount = commentsAsync.await(),
            )
        }
    }

    private suspend fun loadRatingsCount(): Int {
        return coroutineScope {
            with(loadUserRatingsUseCase) {
                listOf(
                    async { if (isMoviesLoaded()) loadLocalMovies() else loadMovies() },
                    async { if (isShowsLoaded()) loadLocalShows() else loadShows() },
                    async { if (isSeasonsLoaded()) loadLocalSeasons() else loadSeasons() },
                    async { if (isEpisodesLoaded()) loadLocalEpisodes() else loadEpisodes() },
                ).awaitAll().sumOf { it.size }
            }
        }
    }

    private suspend fun loadCommentsCount(): Int? {
        return try {
            userRemoteSource.getCommentsCount()
        } catch (error: Exception) {
            error.rethrowCancellation {
                Timber.recordError(error)
            }
            null
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
            playsThisMonth = watchedMovies.sumOf { movie ->
                movie.plays.count { it.isInSameMonthAs(currentDate) }
            },
            allMovies = watchedMovies.size,
            allPlays = watchedMovies.sumOf { it.plays.size },
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
            playsThisMonth = watchedEpisodes.sumOf { episode ->
                episode.plays.count { it.isInSameMonthAs(currentDate) }
            },
            allShows = watchedShows.size,
            allEpisodes = watchedEpisodes.size,
            allPlays = watchedEpisodes.sumOf { it.plays.size },
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
        val playsThisMonth: Int,
        val allMovies: Int,
        val allPlays: Int,
    )

    private data class ShowsCounts(
        val showsThisMonth: Int,
        val playsThisMonth: Int,
        val allShows: Int,
        val allEpisodes: Int,
        val allPlays: Int,
    )
}
