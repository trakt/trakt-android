package tv.trakt.trakt.core.summary.movies.features.related

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.collections.immutable.ImmutableList
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import tv.trakt.trakt.common.helpers.LoadingState
import tv.trakt.trakt.common.helpers.LoadingState.DONE
import tv.trakt.trakt.common.helpers.LoadingState.LOADING
import tv.trakt.trakt.common.helpers.extensions.rethrowCancellation
import tv.trakt.trakt.common.model.Movie
import tv.trakt.trakt.core.summary.movies.features.related.usecases.GetMovieRelatedUseCase

internal class MovieRelatedViewModel(
    private val movie: Movie,
    private val getRelatedMoviesUseCase: GetMovieRelatedUseCase,
) : ViewModel() {
    private val initialState = MovieRelatedState()

    private val itemsState = MutableStateFlow(initialState.items)
    private val loadingState = MutableStateFlow(initialState.loading)
    private val errorState = MutableStateFlow(initialState.error)

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            try {
                loadingState.update { LOADING }

                itemsState.update {
                    getRelatedMoviesUseCase.getRelatedMovies(movie.ids.trakt)
                }
            } catch (error: Exception) {
                error.rethrowCancellation {
                    errorState.update { error }
                    Timber.w(error)
                }
            } finally {
                loadingState.update { DONE }
            }
        }
    }

    @Suppress("UNCHECKED_CAST")
    val state: StateFlow<MovieRelatedState> = combine(
        itemsState,
        loadingState,
        errorState,
    ) { state ->
        MovieRelatedState(
            items = state[0] as ImmutableList<Movie>?,
            loading = state[1] as LoadingState,
            error = state[2] as Exception?,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = initialState,
    )
}
