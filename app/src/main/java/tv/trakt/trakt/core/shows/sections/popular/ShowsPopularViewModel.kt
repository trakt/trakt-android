package tv.trakt.trakt.core.shows.sections.popular

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import tv.trakt.trakt.common.helpers.LoadingState.DONE
import tv.trakt.trakt.common.helpers.LoadingState.LOADING
import tv.trakt.trakt.common.helpers.extensions.rethrowCancellation
import tv.trakt.trakt.core.shows.sections.popular.usecase.GetPopularShowsUseCase

internal class ShowsPopularViewModel(
    private val getPopularUseCase: GetPopularShowsUseCase,
) : ViewModel() {
    private val initialState = ShowsPopularState()

    private val itemsState = MutableStateFlow(initialState.items)
    private val loadingState = MutableStateFlow(initialState.loading)
    private val errorState = MutableStateFlow(initialState.error)

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            val loadingJob = launch {
                delay(250)
                loadingState.update { LOADING }
            }
            try {
                val localShows = getPopularUseCase.getLocalShows()
                if (localShows.isNotEmpty()) {
                    loadingJob.cancel()
                    loadingState.update { DONE }
                    itemsState.update { localShows }
                }

                itemsState.update {
                    getPopularUseCase.getShows()
                }
            } catch (error: Exception) {
                error.rethrowCancellation {
                    errorState.update { error }
                    Timber.e(error, "Failed to load data")
                }
            } finally {
                loadingJob.cancel()
                loadingState.update { DONE }
            }
        }
    }

    val state: StateFlow<ShowsPopularState> = combine(
        loadingState,
        itemsState,
        errorState,
    ) { s1, s2, s3 ->
        ShowsPopularState(
            loading = s1,
            items = s2,
            error = s3,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = initialState,
    )
}
