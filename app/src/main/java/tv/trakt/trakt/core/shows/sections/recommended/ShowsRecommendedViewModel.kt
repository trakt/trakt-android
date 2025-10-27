package tv.trakt.trakt.core.shows.sections.recommended

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
import tv.trakt.trakt.common.helpers.LoadingState.LOADING
import tv.trakt.trakt.common.helpers.extensions.rethrowCancellation
import tv.trakt.trakt.core.shows.sections.recommended.usecase.GetRecommendedShowsUseCase

internal class ShowsRecommendedViewModel(
    private val getRecommendedUseCase: GetRecommendedShowsUseCase,
) : ViewModel() {
    private val initialState = ShowsRecommendedState()

    private val itemsState = MutableStateFlow(initialState.items)
    private val loadingState = MutableStateFlow(initialState.loading)
    private val errorState = MutableStateFlow(initialState.error)

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            try {
                val localShows = getRecommendedUseCase.getLocalShows()
                if (localShows.isNotEmpty()) {
                    itemsState.update { localShows }
                    loadingState.update { DONE }
                } else {
                    loadingState.update { LOADING }
                }

                itemsState.update {
                    getRecommendedUseCase.getShows()
                }
            } catch (error: Exception) {
                error.rethrowCancellation {
                    errorState.update { error }
                    Timber.w(error, "Failed to load data")
                }
            } finally {
                loadingState.update { DONE }
            }
        }
    }

    val state: StateFlow<ShowsRecommendedState> = combine(
        itemsState,
        loadingState,
        errorState,
    ) { s1, s2, s3 ->
        ShowsRecommendedState(
            items = s1,
            loading = s2,
            error = s3,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = initialState,
    )
}
