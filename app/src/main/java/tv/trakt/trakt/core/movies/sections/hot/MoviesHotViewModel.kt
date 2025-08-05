package tv.trakt.trakt.core.movies.sections.hot

import android.util.Log
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
import tv.trakt.trakt.common.helpers.LoadingState.DONE
import tv.trakt.trakt.common.helpers.LoadingState.LOADING
import tv.trakt.trakt.common.helpers.extensions.rethrowCancellation
import tv.trakt.trakt.core.movies.sections.hot.usecase.GetHotMoviesUseCase

internal class MoviesHotViewModel(
    private val getHotUseCase: GetHotMoviesUseCase,
) : ViewModel() {
    private val initialState = MoviesHotState()

    private val itemsState = MutableStateFlow(initialState.items)
    private val loadingState = MutableStateFlow(initialState.loading)
    private val errorState = MutableStateFlow(initialState.error)

    init {
        loadData()
        Log.d("MoviesHotViewModel", "ViewModel initialized")
    }

    private fun loadData() {
        viewModelScope.launch {
            val loadingJob = launch {
                delay(250)
                loadingState.update { LOADING }
            }
            try {
                val movies = getHotUseCase.getHotMovies()
                itemsState.update { movies }
            } catch (error: Exception) {
                error.rethrowCancellation {
                    errorState.update { error }
                    Log.e("MoviesHotViewModel", "Failed to load data", error)
                }
            } finally {
                loadingJob.cancel()
                loadingState.update { DONE }
            }
        }
    }

    val state: StateFlow<MoviesHotState> = combine(
        loadingState,
        itemsState,
        errorState,
    ) { s1, s2, s3 ->
        MoviesHotState(
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
