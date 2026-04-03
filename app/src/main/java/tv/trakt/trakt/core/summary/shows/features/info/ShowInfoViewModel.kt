@file:Suppress("UNCHECKED_CAST")

package tv.trakt.trakt.core.summary.shows.features.info

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.collections.immutable.ImmutableList
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import tv.trakt.trakt.analytics.crashlytics.recordError
import tv.trakt.trakt.common.helpers.LoadingState
import tv.trakt.trakt.common.helpers.LoadingState.Done
import tv.trakt.trakt.common.helpers.LoadingState.Loading
import tv.trakt.trakt.common.helpers.extensions.rethrowCancellation
import tv.trakt.trakt.common.model.Show
import tv.trakt.trakt.common.networking.ShowStatsDto
import tv.trakt.trakt.core.summary.shows.features.info.usecase.GetShowCrewUseCase
import tv.trakt.trakt.core.summary.shows.features.info.usecase.GetShowStatsUseCase
import tv.trakt.trakt.core.summary.shows.features.info.usecase.GetShowStudiosUseCase

internal class ShowInfoViewModel(
    private val show: Show,
    private val getStatsUseCase: GetShowStatsUseCase,
    private val getStudiosUseCase: GetShowStudiosUseCase,
    private val getCrewUseCase: GetShowCrewUseCase,
) : ViewModel() {
    private val initialState = ShowInfoState()

    private val showState = MutableStateFlow(show)
    private val showStatsState = MutableStateFlow(initialState.showStats)
    private val showStudiosState = MutableStateFlow(initialState.showStudios)
    private val showCrewState = MutableStateFlow(initialState.showCrew)
    private val loadingState = MutableStateFlow(initialState.loading)
    private val errorState = MutableStateFlow(initialState.error)

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            try {
                loadingState.update { Loading }

                coroutineScope {
                    awaitAll(
                        async { loadStats() },
                        async { loadStudios() },
                        async { loadCrew() },
                    )
                }

                loadingState.update { Done }
            } catch (error: Exception) {
                error.rethrowCancellation {
                    errorState.update { error }
                }
            }
        }
    }

    private suspend fun loadStudios() {
        try {
            showStudiosState.update {
                getStudiosUseCase.getStudios(show.ids.trakt)
            }
        } catch (error: Exception) {
            error.rethrowCancellation {
                Timber.recordError(error)
            }
        }
    }

    private suspend fun loadStats() {
        try {
            showStatsState.update {
                getStatsUseCase.getStats(show.ids.trakt)
            }
        } catch (error: Exception) {
            error.rethrowCancellation {
                Timber.recordError(error)
            }
        }
    }

    private suspend fun loadCrew() {
        try {
            showCrewState.update {
                getCrewUseCase.getCrew(show.ids.trakt)
            }
        } catch (error: Exception) {
            error.rethrowCancellation {
                Timber.recordError(error)
            }
        }
    }

    val state = combine(
        showState,
        showStatsState,
        showStudiosState,
        showCrewState,
        loadingState,
        errorState,
    ) { state ->
        ShowInfoState(
            show = state[0] as Show?,
            showStats = state[1] as ShowStatsDto?,
            showStudios = state[2] as ImmutableList<String>?,
            showCrew = state[3] as GetShowCrewUseCase.Result?,
            loading = state[4] as LoadingState,
            error = state[5] as Exception?,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = initialState,
    )
}
