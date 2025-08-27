package tv.trakt.trakt.core.home.sections.upcoming

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
import tv.trakt.trakt.core.home.sections.upcoming.usecases.GetUpcomingUseCase

internal class HomeUpcomingViewModel(
    private val getUpcomingUseCase: GetUpcomingUseCase,
) : ViewModel() {
    private val initialState = HomeUpcomingState()

    private val itemsState = MutableStateFlow(initialState.items)
    private val loadingState = MutableStateFlow(initialState.loading)
    private val errorState = MutableStateFlow(initialState.error)

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            try {
                val localItems = getUpcomingUseCase.getLocalUpcoming()
                if (localItems.isNotEmpty()) {
                    itemsState.update { localItems }
                    loadingState.update { DONE }
                } else {
                    loadingState.update { LOADING }
                }

                itemsState.update {
                    getUpcomingUseCase.getUpcoming()
                }
            } catch (error: Exception) {
                errorState.update { error }
                Timber.d(error, "Failed to load upcoming data")
            } finally {
                loadingState.update { DONE }
            }
        }
    }

    val state: StateFlow<HomeUpcomingState> = combine(
        loadingState,
        itemsState,
        errorState,
    ) { s1, s2, s3 ->
        HomeUpcomingState(
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
