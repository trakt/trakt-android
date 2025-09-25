package tv.trakt.trakt.core.home.sections.upnext.context

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
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.core.home.sections.upcoming.data.local.HomeUpcomingLocalDataSource
import tv.trakt.trakt.core.home.sections.upnext.all.data.local.AllUpNextLocalDataSource
import tv.trakt.trakt.core.home.sections.upnext.data.local.HomeUpNextLocalDataSource
import tv.trakt.trakt.core.sync.usecases.UpdateShowHistoryUseCase
import tv.trakt.trakt.core.user.usecase.progress.LoadUserProgressUseCase

internal class UpNextItemContextViewModel(
    private val updateShowHistoryUseCase: UpdateShowHistoryUseCase,
    private val allUpNextLocalDataSource: AllUpNextLocalDataSource,
    private val upNextLocalDataSource: HomeUpNextLocalDataSource,
    private val upcomingLocalDataSource: HomeUpcomingLocalDataSource,
    private val loadUserProgressUseCase: LoadUserProgressUseCase,
) : ViewModel() {
    private val initialState = UpNextItemContextState()

    private val loadingWatchedState = MutableStateFlow(initialState.loadingWatched)
    private val loadingDropState = MutableStateFlow(initialState.loadingDrop)
    private val errorState = MutableStateFlow(initialState.error)

    fun dropShow(showId: TraktId) {
        if (isLoading()) return
        viewModelScope.launch {
            clear()
            try {
                loadingDropState.update { LOADING }
                updateShowHistoryUseCase.dropShow(showId)

                upNextLocalDataSource.removeItems(
                    showIds = listOf(showId),
                    notify = true,
                )

                upcomingLocalDataSource.removeShowItems(
                    showIds = listOf(showId),
                    notify = true,
                )

                allUpNextLocalDataSource.notifyUpdate()
                loadUserProgress()
            } catch (error: Exception) {
                error.rethrowCancellation {
                    errorState.update { error }
                    Timber.w(error)
                }
            } finally {
                loadingDropState.update { DONE }
            }
        }
    }

    fun loadUserProgress() {
        viewModelScope.launch {
            try {
                loadUserProgressUseCase.loadShowsProgress()
            } catch (error: Exception) {
                error.rethrowCancellation {
                    Timber.w(error)
                }
            }
        }
    }

    fun clear() {
        loadingWatchedState.update { IDLE }
        loadingDropState.update { IDLE }
        errorState.update { null }
    }

    private fun isLoading(): Boolean {
        return loadingWatchedState.value.isLoading || loadingDropState.value.isLoading
    }

    val state: StateFlow<UpNextItemContextState> = combine(
        loadingWatchedState,
        loadingDropState,
        errorState,
    ) { s1, s2, s3 ->
        UpNextItemContextState(
            loadingWatched = s1,
            loadingDrop = s2,
            error = s3,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = initialState,
    )
}
