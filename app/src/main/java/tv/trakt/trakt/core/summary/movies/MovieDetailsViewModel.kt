package tv.trakt.trakt.core.summary.movies

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
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
import tv.trakt.trakt.common.model.toTraktId
import tv.trakt.trakt.core.summary.movies.navigation.MovieDetailsDestination
import tv.trakt.trakt.core.summary.movies.usecases.GetMovieDetailsUseCase

internal class MovieDetailsViewModel(
    savedStateHandle: SavedStateHandle,
    private val getMovieDetailsUseCase: GetMovieDetailsUseCase,
) : ViewModel() {
    private val destination = savedStateHandle.toRoute<MovieDetailsDestination>()

    private val initialState = MovieDetailsState()

    private val movieState = MutableStateFlow(initialState.movie)
    private val loadingState = MutableStateFlow(initialState.loading)
    private val errorState = MutableStateFlow(initialState.error)

    init {
        loadMovie()
    }

    private fun loadMovie() {
        viewModelScope.launch {
            try {
                var movie = getMovieDetailsUseCase.getLocalMovie(
                    movieId = destination.movieId.toTraktId(),
                )

                if (movie == null) {
                    loadingState.update { LOADING }
                    movie = getMovieDetailsUseCase.getMovie(
                        movieId = destination.movieId.toTraktId(),
                    )
                }

                movieState.update { movie }
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

    val state: StateFlow<MovieDetailsState> = combine(
        movieState,
        loadingState,
        errorState,
    ) { state ->
        MovieDetailsState(
            movie = state[0] as Movie?,
            loading = state[1] as LoadingState,
            error = state[2] as? Exception,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = initialState,
    )
}
