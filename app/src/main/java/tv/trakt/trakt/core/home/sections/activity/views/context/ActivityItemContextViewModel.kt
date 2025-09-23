package tv.trakt.trakt.core.home.sections.activity.views.context

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import tv.trakt.trakt.common.helpers.LoadingState.DONE
import tv.trakt.trakt.common.helpers.LoadingState.IDLE
import tv.trakt.trakt.common.helpers.LoadingState.LOADING
import tv.trakt.trakt.common.helpers.extensions.rethrowCancellation
import tv.trakt.trakt.core.home.sections.activity.data.local.personal.HomePersonalLocalDataSource
import tv.trakt.trakt.core.sync.usecases.UpdateMovieHistoryUseCase
import tv.trakt.trakt.core.user.usecase.progress.LoadUserProgressUseCase

internal class ActivityItemContextViewModel(
    private val updateMovieHistoryUseCase: UpdateMovieHistoryUseCase,
    private val activityLocalSource: HomePersonalLocalDataSource,
    private val loadUserProgressUseCase: LoadUserProgressUseCase,
) : ViewModel() {
    private val initialState = ActivityItemContextState()

    private val loadingRemoveState = MutableStateFlow(initialState.loadingRemove)
    private val loadingWatchlistState = MutableStateFlow(initialState.loadingWatchlist)
    private val errorState = MutableStateFlow(initialState.error)

    fun removePlayFromHistory(playId: Long) {
        if (isLoading()) return
        viewModelScope.launch {
            clear()
            try {
                loadingRemoveState.update { LOADING }

                updateMovieHistoryUseCase.removePlayFromHistory(playId = playId)
                activityLocalSource.removeItems(ids = setOf(playId), notify = true)

                loadUserProgress()
            } catch (error: Exception) {
                error.rethrowCancellation {
                    errorState.update { error }
                    Timber.w(error)
                }
            } finally {
                loadingRemoveState.update { DONE }
            }
        }
    }

    fun loadUserProgress() {
        viewModelScope.launch {
            try {
                loadUserProgressUseCase.loadAllProgress()
            } catch (error: Exception) {
                error.rethrowCancellation {
                    Timber.w(error)
                }
            }
        }
    }

    fun clear() {
        loadingRemoveState.update { IDLE }
        loadingWatchlistState.update { IDLE }
        errorState.update { null }
    }

    private fun isLoading(): Boolean {
        return loadingRemoveState.value.isLoading || loadingWatchlistState.value.isLoading
    }

    val state: StateFlow<ActivityItemContextState> = combine(
        loadingRemoveState,
        loadingWatchlistState,
        errorState,
    ) { s1, s2, s3 ->
        ActivityItemContextState(
            loadingRemove = s1,
            loadingWatchlist = s2,
            error = s3,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = initialState,
    )
}
