@file:OptIn(FlowPreview::class)

package tv.trakt.trakt.core.profile.sections.screentime

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import tv.trakt.trakt.common.core.user.usecases.progress.updates.ProgressUpdates
import tv.trakt.trakt.common.helpers.LoadingState
import tv.trakt.trakt.common.helpers.LoadingState.Done
import tv.trakt.trakt.common.helpers.LoadingState.Loading
import tv.trakt.trakt.common.helpers.extensions.nowLocalDay
import tv.trakt.trakt.common.helpers.extensions.recordError
import tv.trakt.trakt.common.helpers.extensions.rethrowCancellation
import tv.trakt.trakt.core.profile.sections.screentime.model.ScreenTimeData
import tv.trakt.trakt.core.profile.sections.screentime.usecase.GetScreenTimeUseCase
import tv.trakt.trakt.helpers.collapsing.CollapsingManager
import tv.trakt.trakt.helpers.collapsing.model.CollapsingKey
import kotlin.time.Duration.Companion.milliseconds

internal class ProfileScreenTimeViewModel(
    private val getScreenTimeUseCase: GetScreenTimeUseCase,
    private val collapsingManager: CollapsingManager,
    private val progressUpdates: ProgressUpdates,
) : ViewModel() {
    private val initialState = ProfileScreenTimeState(
        rangeStart = nowLocalDay().minusDays(6),
    )

    private val dataState = MutableStateFlow(initialState.data)
    private val loadingState = MutableStateFlow(initialState.loading)
    private val collapseState = MutableStateFlow(isCollapsed())

    private var collapseJob: Job? = null

    init {
        loadData()
        observeProgress()
    }

    private fun observeProgress() {
        progressUpdates.observeUpdates()
            .distinctUntilChanged()
            .filterNotNull()
            .debounce(300.milliseconds)
            .onEach { loadData() }
            .launchIn(viewModelScope)
    }

    private fun loadData() {
        viewModelScope.launch {
            try {
                loadingState.update { Loading }

                val local = getScreenTimeUseCase.getLocalScreenTimeData()
                dataState.update { local }

                loadingState.update {
                    when (local) {
                        null -> Loading
                        else -> Done
                    }
                }

                dataState.update {
                    getScreenTimeUseCase.getScreenTimeData()
                }
            } catch (error: Exception) {
                error.rethrowCancellation {
                    Timber.recordError(error)
                }
            } finally {
                loadingState.update { Done }
            }
        }
    }

    fun setCollapsed(collapsed: Boolean) {
        collapseState.update { collapsed }

        collapseJob?.cancel()
        collapseJob = viewModelScope.launch {
            when {
                collapsed -> collapsingManager.collapse(CollapsingKey.PROFILE_SCREEN_TIME)
                else -> collapsingManager.expand(CollapsingKey.PROFILE_SCREEN_TIME)
            }
        }
    }

    private fun isCollapsed(): Boolean {
        return collapsingManager.isCollapsed(CollapsingKey.PROFILE_SCREEN_TIME)
    }

    val state = combine(
        dataState,
        loadingState,
        collapseState,
    ) { state ->
        ProfileScreenTimeState(
            rangeStart = initialState.rangeStart,
            data = state[0] as ScreenTimeData?,
            loading = state[1] as LoadingState,
            collapsed = state[2] as Boolean,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = initialState,
    )
}
