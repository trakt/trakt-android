@file:OptIn(FlowPreview::class)

package tv.trakt.trakt.core.home.sections.streaks

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import tv.trakt.trakt.common.helpers.extensions.nowLocalDay
import tv.trakt.trakt.common.helpers.extensions.rethrowCancellation
import tv.trakt.trakt.core.filters.data.GlobalFilterManager
import tv.trakt.trakt.core.home.sections.streaks.data.StreaksManager
import tv.trakt.trakt.core.user.usecases.progress.updates.ProgressUpdates
import kotlin.time.Duration.Companion.milliseconds

internal class HomeStreaksViewModel(
    private val streaksManager: StreaksManager,
    private val filtersManager: GlobalFilterManager,
    private val progressUpdates: ProgressUpdates,
) : ViewModel() {
    private var dataJob: Job? = null

    init {
        observeMode()
        observeProgress()
        loadData()
    }

    private fun observeProgress() {
        progressUpdates.observeUpdates()
            .distinctUntilChanged()
            .filterNotNull()
            .debounce(300.milliseconds)
            .onEach { loadData() }
            .launchIn(viewModelScope)
    }

    private fun observeMode() {
        filtersManager.observeFilter()
            .onEach { loadData() }
            .launchIn(viewModelScope)
    }

    private fun loadData() {
        dataJob?.cancel()
        dataJob = viewModelScope.launch {
            try {
                streaksManager.loadStreakData(
                    localDay = nowLocalDay(),
                    mode = filtersManager.getFilter().mode,
                )
            } catch (error: Exception) {
                error.rethrowCancellation { }
            }
        }
    }

    val state = streaksManager.observeStreakData()
        .map { HomeStreaksState(data = it) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = HomeStreaksState(),
        )
}
