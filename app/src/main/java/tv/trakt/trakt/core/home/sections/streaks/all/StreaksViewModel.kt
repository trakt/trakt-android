package tv.trakt.trakt.core.home.sections.streaks.all

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import tv.trakt.trakt.common.helpers.LoadingState.Idle
import tv.trakt.trakt.common.model.MediaMode
import tv.trakt.trakt.core.filters.data.GlobalFilterManager
import tv.trakt.trakt.core.home.sections.streaks.data.StreaksManager
import tv.trakt.trakt.core.home.sections.streaks.model.MonthlyStreakData

internal class StreaksViewModel(
    streaksManager: StreaksManager,
    private val filtersManager: GlobalFilterManager,
) : ViewModel() {
    val state = combine(
        streaksManager.observeStreakData(),
        filtersManager.observeFilter()
    ) { data, filter ->
        StreaksState(
            mode = filter.mode,
            data = data,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = StreaksState(),
    )
}
