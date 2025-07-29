package tv.trakt.trakt.core.shows.sections.trending

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import tv.trakt.trakt.common.helpers.extensions.rethrowCancellation
import tv.trakt.trakt.core.shows.sections.trending.usecase.GetTrendingShowsUseCase

internal class ShowsTrendingViewModel(
    private val getTrendingUseCase: GetTrendingShowsUseCase,
) : ViewModel() {
    private val initialState = ShowsTrendingState()

    private val itemsState = MutableStateFlow(initialState.items)
    private val loadingState = MutableStateFlow(initialState.loading)
    private val errorState = MutableStateFlow(initialState.error)

    init {
        loadData()
        Log.d("ShowsTrendingViewModel", "ViewModel initialized")
    }

    private fun loadData() {
        viewModelScope.launch {
            try {
                loadingState.update { true }
                val shows = getTrendingUseCase.getTrendingShows()
                itemsState.update { shows }
            } catch (error: Exception) {
                error.rethrowCancellation {
                    Log.e("ShowsTrendingViewModel", "Failed to load data", error)
                    errorState.update { error }
                }
            } finally {
                loadingState.update { false }
            }
        }
    }

    val state: StateFlow<ShowsTrendingState> = combine(
        loadingState,
        itemsState,
        errorState,
    ) { s1, s2, s3 ->
        ShowsTrendingState(
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
