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
import tv.trakt.trakt.core.home.sections.upcoming.usecases.GetUpcomingUseCase

internal class HomeUpcomingViewModel(
    private val getUpcomingUseCase: GetUpcomingUseCase,
) : ViewModel() {
    private val initialState = HomeUpcomingState()

    private val itemsState = MutableStateFlow(initialState.items)
    private val loadingState = MutableStateFlow(initialState.isLoading)
    private val errorState = MutableStateFlow(initialState.error)

    init {
        loadData()
    }

    private fun loadData(showLoading: Boolean = true) {
        viewModelScope.launch {
            try {
                if (showLoading) {
                    loadingState.update { true }
                }

                val items = getUpcomingUseCase.getCalendar()
                itemsState.update { items }
            } catch (error: Exception) {
                Timber.e(error, "Failed to load upcoming data")
                errorState.update { error }
            } finally {
                loadingState.update { false }
            }
        }
    }

    fun updateData() {
        Timber.d("updateData called")
        loadData(showLoading = false)
    }

    val state: StateFlow<HomeUpcomingState> = combine(
        loadingState,
        itemsState,
        errorState,
    ) { s1, s2, s3 ->
        HomeUpcomingState(
            isLoading = s1,
            items = s2,
            error = s3,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = initialState,
    )
}
