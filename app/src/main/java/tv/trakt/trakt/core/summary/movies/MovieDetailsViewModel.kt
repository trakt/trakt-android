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
import tv.trakt.trakt.common.model.ExternalRating
import tv.trakt.trakt.common.model.Movie
import tv.trakt.trakt.common.model.toTraktId
import tv.trakt.trakt.core.summary.movies.navigation.MovieDetailsDestination
import tv.trakt.trakt.core.summary.movies.usecases.GetMovieDetailsUseCase
import tv.trakt.trakt.core.summary.movies.usecases.GetMovieRatingsUseCase

internal class MovieDetailsViewModel(
    savedStateHandle: SavedStateHandle,
    private val getDetailsUseCase: GetMovieDetailsUseCase,
    private val getExternalRatingsUseCase: GetMovieRatingsUseCase,
) : ViewModel() {
    private val destination = savedStateHandle.toRoute<MovieDetailsDestination>()
    private val movieId = destination.movieId.toTraktId()

    private val initialState = MovieDetailsState()

    private val movieState = MutableStateFlow(initialState.movie)
    private val movieRatingsState = MutableStateFlow(initialState.movieRatings)
    private val loadingState = MutableStateFlow(initialState.loading)
    private val errorState = MutableStateFlow(initialState.error)

    init {
        loadMovie()
    }

    private fun loadMovie() {
        viewModelScope.launch {
            try {
                var movie = getDetailsUseCase.getLocalMovie(
                    movieId = movieId,
                )

                if (movie == null) {
                    loadingState.update { LOADING }
                    movie = getDetailsUseCase.getMovie(
                        movieId = movieId,
                    )
                }

                movieState.update { movie }
                loadRatings()
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

    private fun loadRatings() {
        viewModelScope.launch {
            try {
                movieRatingsState.update {
                    getExternalRatingsUseCase.getExternalRatings(movieId)
                }
            } catch (error: Exception) {
                error.rethrowCancellation {
                    Timber.w(error)
                }
            }
        }
    }

    val state: StateFlow<MovieDetailsState> = combine(
        movieState,
        movieRatingsState,
        loadingState,
        errorState,
    ) { state ->
        MovieDetailsState(
            movie = state[0] as Movie?,
            movieRatings = state[1] as ExternalRating?,
            loading = state[2] as LoadingState,
            error = state[3] as? Exception,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = initialState,
    )
}
