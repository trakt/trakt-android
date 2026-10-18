package tv.trakt.trakt.core.filters

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import tv.trakt.trakt.common.firebase.analytics.Analytics
import tv.trakt.trakt.common.model.globalfilter.GlobalFilter
import tv.trakt.trakt.common.model.globalfilter.GlobalFilterMode
import tv.trakt.trakt.common.model.globalfilter.GlobalFilterMode.Advanced
import tv.trakt.trakt.common.model.globalfilter.GlobalFilterMode.Simple
import tv.trakt.trakt.core.filters.data.GlobalFilterManager
import tv.trakt.trakt.core.filters.navigation.GlobalFiltersOptions

internal class GlobalFiltersViewModel(
    private val options: GlobalFiltersOptions,
    private val filterManager: GlobalFilterManager,
    private val analytics: Analytics,
) : ViewModel() {
    private val initialState = GlobalFiltersState(
        mode = filterManager.getMode(),
        filter = options.initial ?: filterManager.getFilter(),
    )
    private val modeState = MutableStateFlow(initialState.mode)
    private val filterState = MutableStateFlow(initialState.filter)

    fun applyFilter(filter: GlobalFilter) {
        viewModelScope.launch {
            val previous = filterState.value
            filterState.update { filter }
            if (options.global) {
                filterManager.setFilter(filter)
            }
            logFilterChanges(previous = previous, current = filter)
        }
    }

    private fun logFilterChanges(
        previous: GlobalFilter,
        current: GlobalFilter,
    ) {
        val mode = modeState.value ?: return
        current.changesFrom(previous).forEach { change ->
            analytics.logFilter(
                id = change.key,
                set = change.set,
                mode = mode.name,
            )
        }
    }

    fun applyMode(mode: GlobalFilterMode) {
        viewModelScope.launch {
            val currentMode = modeState.value

            if (currentMode == Advanced && mode == Simple) {
                with(
                    GlobalFilter.Default.copy(
                        mode = filterState.value.mode,
                    ),
                ) {
                    filterState.update { this }
                    if (options.global) {
                        filterManager.setFilter(this)
                    }
                }
            }

            modeState.update { mode }
            if (options.global) {
                filterManager.setMode(mode)
            }
        }
    }

    val state = combine(
        filterState,
        modeState,
    ) { state ->
        GlobalFiltersState(
            filter = state[0] as GlobalFilter,
            mode = state[1] as GlobalFilterMode?,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = initialState,
    )
}
