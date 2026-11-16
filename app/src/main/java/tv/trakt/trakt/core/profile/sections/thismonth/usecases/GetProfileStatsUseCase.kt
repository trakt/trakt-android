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
import tv.trakt.trakt.core.profile.sections.thismonth.model.ProfileStats
import java.time.Instant
import java.time.ZonedDateTime

internal class GetProfileStatsUseCase(
    private val loadUserProgressUseCase: LoadUserProgressUseCase,
    private val loadUserRatingsUseCase: LoadUserRatingsUseCase,
    private val userRemoteSource: UserRemoteSource,
    private val sessionManager: SessionManager,
) {
    suspend fun getProfileStats(): ProfileStats {
        return coroutineScope {
            val currentDate = nowLocal()
            val isVip = sessionManager.getProfile()?.isAnyVip == true

            val showsAsync = async { loadShowsEpisodesCount(currentDate) }
            val moviesAsync = async { loadMoviesCount(currentDate) }
            val ratingsAsync = async { if (isVip) 0 else loadRatingsCount() }
            val commentsAsync = async { if (isVip) 0 else loadCommentsCount() }

            val shows = showsAsync.await()
            val movies = moviesAsync.await()
            val allPlays = shows.allPlays + movies.allPlays

            ProfileStats(
                showsCount = shows.showsThisMonth,
                episodesCount = shows.episodesThisMonth,
                moviesCount = movies.moviesThisMonth,
                playsCount = shows.playsThisMonth + movies.playsThisMonth,
                allShowsCount = shows.allShows,
                allEpisodesCount = shows.allEpisodes,
                allMoviesCount = movies.allMovies,
                allPlaysCount = allPlays,
                allTime = if (isVip) {
                    null
                } else {
                    UserStats(
                        totalMinutes = null,
                        totalPlays = allPlays,
                        moviesWatched = movies.allMovies,
                        showsWatched = shows.allShows,
                        episodesWatched = shows.allEpisodes,
                        ratings = ratingsAsync.await(),
                        comments = commentsAsync.await(),
                    )
                },
            )
        }
    }

    /**
     * Full all-time stats from the API. The API only has them for VIP accounts, so this is null
     * for everyone else and [getProfileStats] builds them from local data instead.
     */
    suspend fun getAllTimeStats(): UserStats? {
        val user = sessionManager.getProfile() ?: return null
        if (!user.isAnyVip) return null

        return userRemoteSource.getStats(userId = "me")?.let(UserStats::fromDto)
    }

    private suspend fun loadRatingsCount(): Int {
        return try {
            coroutineScope {
                with(loadUserRatingsUseCase) {
                    listOf(
                        async { if (isMoviesLoaded()) loadLocalMovies() else loadMovies() },
                        async { if (isShowsLoaded()) loadLocalShows() else loadShows() },
                        async { if (isSeasonsLoaded()) loadLocalSeasons() else loadSeasons() },
                        async { if (isEpisodesLoaded()) loadLocalEpisodes() else loadEpisodes() },
                    ).awaitAll().sumOf { it.size }
                }
            }
        } catch (error: Exception) {
            error.rethrowCancellation { Timber.recordError(error) }
            0
        }
    }

    private suspend fun loadCommentsCount(): Int {
        return try {
            userRemoteSource.getCommentsCount() ?: 0
        } catch (error: Exception) {
            error.rethrowCancellation { Timber.recordError(error) }
            0
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
            playsThisMonth = watchedMovies.sumOf { movie ->
                movie.plays.count { it.isInSameMonthAs(currentDate) }
            },
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
            episodesThisMonth = watchedEpisodes.count { it.wasWatchedIn(currentDate) },
            allShows = watchedShows.size,
            allEpisodes = watchedEpisodes.size,
            playsThisMonth = watchedEpisodes.sumOf { episode ->
                episode.plays.count { it.isInSameMonthAs(currentDate) }
            },
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
        val moviesThisMonth: Int,
        val allMovies: Int,
        val playsThisMonth: Int,
        val allPlays: Int,
    )

    private data class ShowsCounts(
        val showsThisMonth: Int,
        val episodesThisMonth: Int,
        val allShows: Int,
        val allEpisodes: Int,
        val playsThisMonth: Int,
        val allPlays: Int,
    )
}
