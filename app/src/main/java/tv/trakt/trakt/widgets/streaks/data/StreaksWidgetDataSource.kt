package tv.trakt.trakt.widgets.streaks.data

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.first
import timber.log.Timber
import tv.trakt.trakt.common.helpers.extensions.nowLocalDay
import tv.trakt.trakt.common.model.MediaMode
import tv.trakt.trakt.core.home.sections.streaks.data.StreaksManager
import tv.trakt.trakt.widgets.streaks.StreaksWidgetDay
import tv.trakt.trakt.widgets.streaks.StreaksWidgetState
import java.time.DayOfWeek

internal class StreaksWidgetDataSource(
    private val streaksManager: StreaksManager,
) {
    private val loadedState = mutableStateOf(StreaksWidgetState())

    /**
     * Snapshot state, not a plain value: Glance hands its content lambda to the session once, so
     * everything that changes after the first frame has to be read from the composition.
     */
    val state: StreaksWidgetState
        @Composable get() = loadedState.value

    suspend fun refresh() {
        val state = loadRemote()
        // A failed refresh keeps the streak already on screen; only a first load can show the error.
        if (!state.error || !loadedState.value.loaded) {
            loadedState.value = state
        }
    }

    private suspend fun loadRemote(): StreaksWidgetState {
        val today = nowLocalDay()

        runCatching {
            streaksManager.loadStreakData(
                localDay = today,
                mode = MediaMode.Media,
            )
        }.getOrElse { error ->
            Timber.w(error, "Failed to load Streaks widget data")
            return StreaksWidgetState(error = true)
        }

        val data = streaksManager.observeStreakData().first()

        val startOfWeek = today.with(DayOfWeek.MONDAY)
        val week = (0..6)
            .map { offset ->
                val date = startOfWeek.plusDays(offset.toLong())
                StreaksWidgetDay(
                    active = (data.activity[date]?.total ?: 0) > 0,
                    today = date == today,
                    future = date.isAfter(today),
                )
            }
            .toImmutableList()

        return StreaksWidgetState(
            streakDays = data.currentStreakTotal,
            week = week,
            loaded = true,
        )
    }
}
