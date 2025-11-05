package tv.trakt.trakt.core.movies.sections.anticipated

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
import tv.trakt.trakt.core.movies.sections.anticipated.usecase.GetAnticipatedMoviesUseCase

internal class MoviesAnticipatedViewModel(
    private val getAnticipatedUseCase: GetAnticipatedMoviesUseCase,
) : ViewModel() {
    private val initialState = MoviesAnticipatedState()

    private val itemsState = MutableStateFlow(initialState.items)
    private val loadingState = MutableStateFlow(initialState.loading)
    private val errorState = MutableStateFlow(initialState.error)

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            try {
                val localMovies = getAnticipatedUseCase.getLocalMovies()
                if (localMovies.isNotEmpty()) {
                    itemsState.update { localMovies }
                    loadingState.update { DONE }
                } else {
                    loadingState.update { LOADING }
                }

                itemsState.update {
                    getAnticipatedUseCase.getMovies()
                }
            } catch (error: Exception) {
                error.rethrowCancellation {
                    errorState.update { error }
                    Timber.e(error, "Failed to load data")
                }
            } finally {
                loadingState.update { DONE }
            }
        }
    }

    val state: StateFlow<MoviesAnticipatedState> = combine(
        loadingState,
        itemsState,
        errorState,
    ) { s1, s2, s3 ->
        MoviesAnticipatedState(
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
