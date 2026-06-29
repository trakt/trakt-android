package tv.trakt.trakt.core.home.sections.streaks.data

import kotlinx.collections.immutable.toImmutableMap
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.update
import tv.trakt.trakt.common.helpers.extensions.EmptyImmutableList
import tv.trakt.trakt.common.helpers.extensions.toLocalDay
import tv.trakt.trakt.common.model.MediaMode
import tv.trakt.trakt.common.model.MediaMode.Media
import tv.trakt.trakt.common.model.MediaMode.Movies
import tv.trakt.trakt.common.model.MediaMode.Shows
import tv.trakt.trakt.core.home.sections.streaks.model.MonthlyStreakData
import tv.trakt.trakt.core.user.usecases.progress.LoadUserProgressUseCase
import java.time.LocalDate

internal class DefaultStreaksManager(
    private val userProgressUseCase: LoadUserProgressUseCase,
) : StreaksManager {
    private val _stateFlow = MutableStateFlow<MonthlyStreakData?>(null)
    val stateFlow = _stateFlow.asStateFlow()

    override suspend fun loadStreakData(
        localDay: LocalDate,
        mode: MediaMode,
    ) {
        if (!userProgressUseCase.isLoaded()) {
            userProgressUseCase.loadProgress()
        }

        coroutineScope {
            val watchedShowsAsync = async {
                when (mode) {
                    Movies -> EmptyImmutableList
                    Media -> userProgressUseCase.loadLocalShows()
                    Shows -> userProgressUseCase.loadLocalShows()
                }
            }
            val watchedMoviesAsync = async {
                when (mode) {
                    Shows -> EmptyImmutableList
                    Media -> userProgressUseCase.loadLocalMovies()
                    Movies -> userProgressUseCase.loadLocalMovies()
                }
            }

            val watchedShows = watchedShowsAsync.await()
            val watchedMovies = watchedMoviesAsync.await()

            val movieDates: List<LocalDate> = watchedMovies.flatMap { movie ->
                movie.plays.map { it.toLocalDay() }
            }

            val episodeDates = watchedShows.flatMap { show ->
                show.seasons.flatMap { season ->
                    season.episodes
                        .flatMap { episode ->
                            episode.plays.map { it.toLocalDay() }
                        }
                }
            }

            val allActivityDates = (movieDates + episodeDates).toSet()
            val activeDaysYear = allActivityDates.count { it.year == localDay.year }

            val movieCountsByDateMonth = movieDates
                .filter { it.month == localDay.month && it.year == localDay.year }
                .groupingBy { it }
                .eachCount()

            val episodeCountsByDateMonth = episodeDates
                .filter { it.month == localDay.month && it.year == localDay.year }
                .groupingBy { it }
                .eachCount()

            val monthDates = movieCountsByDateMonth.keys + episodeCountsByDateMonth.keys
            val monthActivityMap: Map<LocalDate, MonthlyStreakData.StreakDataPoint> = monthDates
                .associateWith { date ->
                    MonthlyStreakData.StreakDataPoint(
                        episodes = episodeCountsByDateMonth.getOrDefault(date, 0),
                        movies = movieCountsByDateMonth.getOrDefault(date, 0),
                    )
                }

            val currentStreakTotal = computeCurrentStreak(allActivityDates, localDay)
            val previousStreakTotal = computePreviousStreak(allActivityDates, localDay, currentStreakTotal)
            val currentStreak = computeCurrentStreak(monthActivityMap.keys, localDay)
            val previousStreak = computePreviousStreak(monthActivityMap.keys, localDay, currentStreak)
            val activeDaysMonth = monthActivityMap.size

            val streakData = MonthlyStreakData(
                activity = monthActivityMap.toImmutableMap(),
                currentStreakTotal = currentStreakTotal,
                previousStreakTotal = previousStreakTotal,
                currentStreak = currentStreak,
                previousStreak = previousStreak,
                droppedStreaks = computeDroppedStreaks(monthActivityMap.keys, localDay),
                activeDaysMonth = activeDaysMonth,
                activeDaysMonthPercent = (activeDaysMonth * 100) / localDay.dayOfMonth,
                activeDaysYear = activeDaysYear,
            )

            _stateFlow.update { streakData }
        }
    }

    override fun observeStreakData(): Flow<MonthlyStreakData> = stateFlow.filterNotNull()

    private fun computeCurrentStreak(
        activitySet: Set<LocalDate>,
        today: LocalDate,
    ): Int {
        var streak = 0
        // Today not yet over — streak stays alive if yesterday was active
        var day = if (today in activitySet) today else today.minusDays(1)
        while (day in activitySet) {
            streak++
            day = day.minusDays(1)
        }
        return streak
    }

    private fun computePreviousStreak(
        activitySet: Set<LocalDate>,
        today: LocalDate,
        currentStreak: Int,
    ): Int {
        // When today is inactive the current streak anchors to yesterday, so shift by 1 extra
        val offset = if (today in activitySet) currentStreak else currentStreak + 1
        var day = today.minusDays(offset.toLong())
        while (day !in activitySet && day.isAfter(today.minusYears(1))) {
            day = day.minusDays(1)
        }
        var streak = 0
        while (day in activitySet) {
            streak++
            day = day.minusDays(1)
        }
        return streak
    }

    private fun computeDroppedStreaks(
        activitySet: Set<LocalDate>,
        today: LocalDate,
    ): Int {
        val monthStart = today.withDayOfMonth(1)
        var drops = 0
        var prevWasActive = false
        var day = monthStart
        val yesterday = today.minusDays(1)
        while (!day.isAfter(yesterday)) {
            val isActive = day in activitySet
            if (prevWasActive && !isActive) drops++
            prevWasActive = isActive
            day = day.plusDays(1)
        }
        return drops
    }
}
