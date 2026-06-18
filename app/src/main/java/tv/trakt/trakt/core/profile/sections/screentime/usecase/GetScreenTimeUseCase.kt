package tv.trakt.trakt.core.profile.sections.screentime.usecase

import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.toImmutableMap
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import tv.trakt.trakt.common.core.user.data.remote.history.UserHistoryRemoteDataSource
import tv.trakt.trakt.common.helpers.extensions.nowLocalDay
import tv.trakt.trakt.common.helpers.extensions.toInstant
import tv.trakt.trakt.common.helpers.extensions.toLocalDay
import tv.trakt.trakt.common.helpers.extensions.toLocalTime
import tv.trakt.trakt.common.networking.SyncHistoryEpisodeItemDto
import tv.trakt.trakt.common.networking.SyncHistoryMovieItemDto
import tv.trakt.trakt.core.profile.sections.screentime.data.local.ScreenTimeLocalDataSource
import tv.trakt.trakt.core.profile.sections.screentime.model.ScreenTimeData
import java.time.LocalDate
import java.time.ZoneOffset.UTC
import kotlin.math.roundToInt
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes
import kotlin.time.toKotlinInstant

internal const val WAKING_HOURS_PER_DAY = 16

internal class GetScreenTimeUseCase(
    private val remoteSource: UserHistoryRemoteDataSource,
    private val localDataSource: ScreenTimeLocalDataSource,
) {
    suspend fun getLocalScreenTimeData(): ScreenTimeData? {
        return localDataSource.getData()
    }

    suspend fun getScreenTimeData(): ScreenTimeData {
        val startLocal = nowLocalDay().minusDays(14).atStartOfDay()
        val endLocal = nowLocalDay().plusDays(1).atStartOfDay()

        return coroutineScope {
            val from = startLocal.toInstant(UTC).toKotlinInstant()
            val to = endLocal.toInstant(UTC).toKotlinInstant()

            val remoteEpisodesAsync = async {
                remoteSource.getEpisodesHistory(
                    limit = 500,
                    filters = null,
                    from = from,
                    to = to,
                )
            }

            val remoteMoviesAsync = async {
                remoteSource.getMoviesHistory(
                    limit = 500,
                    filters = null,
                    from = from,
                    to = to,
                )
            }

            val remoteEpisodes = remoteEpisodesAsync.await()
            val remoteMovies = remoteMoviesAsync.await()

            val offsetCurrent = nowLocalDay().minusDays(6)
            val offsetPrevious = offsetCurrent.minusDays(7)

            val episodesLast7Days = remoteEpisodes.filter { it.watchedAt.toInstant().toLocalDay() >= offsetCurrent }
            val moviesLast7Days = remoteMovies.filter { it.watchedAt.toInstant().toLocalDay() >= offsetCurrent }

            val episodesPrevious7Days = remoteEpisodes.filter {
                val watchedDay = it.watchedAt.toInstant().toLocalDay()
                watchedDay in offsetPrevious..<offsetCurrent
            }
            val moviesPrevious7Days = remoteMovies.filter {
                val watchedDay = it.watchedAt.toInstant().toLocalDay()
                watchedDay in offsetPrevious..<offsetCurrent
            }

            val (totalTime, totalTimePrevious) = getTotalTime(
                episodes = episodesLast7Days,
                previousEpisodes = episodesPrevious7Days,
                movies = moviesLast7Days,
                previousMovies = moviesPrevious7Days,
            )

            val (showsTime, showsPreviousTime) = getTotalShowsTime(
                episodes = episodesLast7Days,
                previousEpisodes = episodesPrevious7Days,
            )

            val (moviesTime, moviesPreviousTime) = getTotalMoviesTime(
                movies = moviesLast7Days,
                previousMovies = moviesPrevious7Days,
            )

            val (averagePerDay, averagePerDayPrevious) = getTotalAvgTime(
                totalTime = totalTime,
                totalPreviousTime = totalTimePrevious,
                episodes = episodesLast7Days,
                previousEpisodes = episodesPrevious7Days,
                movies = moviesLast7Days,
                previousMovies = moviesPrevious7Days,
            )

            val (wakingHoursPercent, wakingHoursPercentChange) = getWakingHoursPercent(
                totalTime = totalTime,
                totalPreviousTime = totalTimePrevious,
            )

            return@coroutineScope ScreenTimeData(
                stats = ScreenTimeData.Stats(
                    totalTime = totalTime,
                    totalTimeChange = totalTime - totalTimePrevious,
                    averagePerDay = averagePerDay,
                    averagePerDayChange = averagePerDay - averagePerDayPrevious,
                    showsTime = showsTime,
                    showsTimeChange = showsTime - showsPreviousTime,
                    moviesTime = moviesTime,
                    moviesTimeChange = moviesTime - moviesPreviousTime,
                    wakingHoursPercent = wakingHoursPercent,
                    wakingHoursPercentChange = wakingHoursPercentChange,
                ),
                dailyHours = getDailyHours(
                    rangeStart = offsetCurrent,
                    episodes = episodesLast7Days,
                    movies = moviesLast7Days,
                ),
                peakHours = getPeakHours(
                    episodes = episodesLast7Days,
                    movies = moviesLast7Days,
                ),
            )
        }.also {
            localDataSource.setData(it)
        }
    }

    private fun getDailyHours(
        rangeStart: LocalDate,
        episodes: List<SyncHistoryEpisodeItemDto>,
        movies: List<SyncHistoryMovieItemDto>,
    ): ImmutableMap<LocalDate, Duration> {
        val minutesPerDay = mutableMapOf<LocalDate, Int>()
        episodes.forEach { item ->
            val day = item.watchedAt.toInstant().toLocalDay()
            minutesPerDay[day] = (minutesPerDay[day] ?: 0) + (item.episode.runtime ?: 0)
        }
        movies.forEach { item ->
            val day = item.watchedAt.toInstant().toLocalDay()
            minutesPerDay[day] = (minutesPerDay[day] ?: 0) + (item.movie.runtime ?: 0)
        }

        return (0L..6L)
            .associate { offset ->
                val day = rangeStart.plusDays(offset)
                day to (minutesPerDay[day] ?: 0).minutes
            }
            .toImmutableMap()
    }

    private fun getPeakHours(
        episodes: List<SyncHistoryEpisodeItemDto>,
        movies: List<SyncHistoryMovieItemDto>,
    ): ImmutableMap<ScreenTimeData.PeakHour, Int> {
        val watchedTimes = episodes.map { it.watchedAt.toInstant().toLocalTime() } +
            movies.map { it.watchedAt.toInstant().toLocalTime() }

        return ScreenTimeData.PeakHour.entries
            .associateWith { peakHour ->
                watchedTimes.count { time ->
                    val (start, end) = peakHour.period
                    when {
                        start <= end -> time in start..end
                        else -> time >= start || time <= end
                    }
                }
            }
            .toImmutableMap()
    }

    private fun getWakingHoursPercent(
        totalTime: Duration,
        totalPreviousTime: Duration,
    ): Pair<Int, Int> {
        val wakingMinutes = WAKING_HOURS_PER_DAY * 7 * 60
        val thisPercent = (totalTime.inWholeMinutes.toDouble() / wakingMinutes * 100).roundToInt()
        val previousPercent = (totalPreviousTime.inWholeMinutes.toDouble() / wakingMinutes * 100).roundToInt()
        return thisPercent to (thisPercent - previousPercent)
    }

    private fun getTotalTime(
        episodes: List<SyncHistoryEpisodeItemDto>,
        previousEpisodes: List<SyncHistoryEpisodeItemDto>,
        movies: List<SyncHistoryMovieItemDto>,
        previousMovies: List<SyncHistoryMovieItemDto>,
    ): Pair<Duration, Duration> {
        val totalTime =
            episodes.sumOf { it.episode.runtime ?: 0 } +
                movies.sumOf { it.movie.runtime ?: 0 }

        val totalPreviousTime = previousEpisodes.sumOf { it.episode.runtime ?: 0 } +
            previousMovies.sumOf { it.movie.runtime ?: 0 }

        return totalTime.minutes to totalPreviousTime.minutes
    }

    private fun getTotalAvgTime(
        totalTime: Duration,
        totalPreviousTime: Duration,
        episodes: List<SyncHistoryEpisodeItemDto>,
        previousEpisodes: List<SyncHistoryEpisodeItemDto>,
        movies: List<SyncHistoryMovieItemDto>,
        previousMovies: List<SyncHistoryMovieItemDto>,
    ): Pair<Duration, Duration> {
        val activeDays = activeDayCount(episodes, movies)
        val activeDaysPrevious = activeDayCount(previousEpisodes, previousMovies)

        val average = if (activeDays > 0) totalTime / activeDays else Duration.ZERO
        val averagePrevious = if (activeDaysPrevious > 0) totalPreviousTime / activeDaysPrevious else Duration.ZERO

        return average to averagePrevious
    }

    private fun activeDayCount(
        episodes: List<SyncHistoryEpisodeItemDto>,
        movies: List<SyncHistoryMovieItemDto>,
    ): Int {
        val days = episodes.mapTo(mutableSetOf()) { it.watchedAt.toInstant().toLocalDay() }
        movies.mapTo(days) { it.watchedAt.toInstant().toLocalDay() }
        return days.size
    }

    private fun getTotalShowsTime(
        episodes: List<SyncHistoryEpisodeItemDto>,
        previousEpisodes: List<SyncHistoryEpisodeItemDto>,
    ): Pair<Duration, Duration> {
        val showsTime = episodes.sumOf { it.episode.runtime ?: 0 }
        val showsPreviousTime = previousEpisodes.sumOf { it.episode.runtime ?: 0 }
        return showsTime.minutes to showsPreviousTime.minutes
    }

    private fun getTotalMoviesTime(
        movies: List<SyncHistoryMovieItemDto>,
        previousMovies: List<SyncHistoryMovieItemDto>,
    ): Pair<Duration, Duration> {
        val moviesTime = movies.sumOf { it.movie.runtime ?: 0 }
        val moviesPreviousTime = previousMovies.sumOf { it.movie.runtime ?: 0 }
        return moviesTime.minutes to moviesPreviousTime.minutes
    }
}
