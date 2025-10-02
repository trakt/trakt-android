package tv.trakt.trakt.core.summary.movies

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import kotlinx.collections.immutable.ImmutableList
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import tv.trakt.trakt.common.auth.session.SessionManager
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
import tv.trakt.trakt.core.summary.movies.usecases.GetMovieStudiosUseCase
import tv.trakt.trakt.core.user.usecase.progress.LoadUserProgressUseCase
import tv.trakt.trakt.core.user.usecase.watchlist.LoadUserWatchlistUseCase

internal class MovieDetailsViewModel(
    savedStateHandle: SavedStateHandle,
    private val getDetailsUseCase: GetMovieDetailsUseCase,
    private val getExternalRatingsUseCase: GetMovieRatingsUseCase,
    private val getMovieStudiosUseCase: GetMovieStudiosUseCase,
    private val loadProgressUseCase: LoadUserProgressUseCase,
    private val loadWatchlistUseCase: LoadUserWatchlistUseCase,
    private val sessionManager: SessionManager,
) : ViewModel() {
    private val destination = savedStateHandle.toRoute<MovieDetailsDestination>()
    private val movieId = destination.movieId.toTraktId()

    private val initialState = MovieDetailsState()

    private val movieState = MutableStateFlow(initialState.movie)
    private val movieRatingsState = MutableStateFlow(initialState.movieRatings)
    private val movieStudiosState = MutableStateFlow(initialState.movieStudios)
    private val movieProgressState = MutableStateFlow(initialState.movieProgress)
    private val loadingState = MutableStateFlow(initialState.loading)
    private val loadingProgress = MutableStateFlow(initialState.loadingProgress)
    private val errorState = MutableStateFlow(initialState.error)

    init {
        loadData()
        loadProgressData()
    }

    private fun loadData() {
        viewModelScope.launch {
            try {
                var movie = getDetailsUseCase.getLocalMovie(movieId)
                if (movie == null) {
                    loadingState.update { LOADING }
                    movie = getDetailsUseCase.getMovie(
                        movieId = movieId,
                    )
                }
                movieState.update { movie }

                loadRatings()
                loadStudios()
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

    private fun loadProgressData() {
        viewModelScope.launch {
            if (!sessionManager.isAuthenticated()) {
                return@launch
            }
            try {
                loadingProgress.update { LOADING }
                coroutineScope {
                    val progressAsync = async {
                        if (!loadProgressUseCase.isMoviesLoaded()) {
                            loadProgressUseCase.loadMoviesProgress()
                        }
                    }
                    val watchlistAsync = async {
                        if (!loadWatchlistUseCase.isMoviesLoaded()) {
                            loadWatchlistUseCase.loadWatchlist()
                        }
                    }
                    progressAsync.await()
                    watchlistAsync.await()
                }

                coroutineScope {
                    val progressAsync = async {
                        loadProgressUseCase.loadLocalMovies()
                            .firstOrNull {
                                it.movie.ids.trakt == movieId
                            }
                    }

                    val watchlistAsync = async {
                        loadWatchlistUseCase.loadLocalMovies()
                            .firstOrNull {
                                it.movie.ids.trakt == movieId
                            }
                    }

                    val progress = progressAsync.await()
                    val watchlist = watchlistAsync.await()

                    movieProgressState.update {
                        MovieDetailsState.ProgressState(
                            plays = progress?.plays ?: 0,
                            watchlist = watchlist != null,
                        )
                    }
                }
            } catch (error: Exception) {
                error.rethrowCancellation {
                    Timber.w(error)
                }
            } finally {
                loadingProgress.update { DONE }
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

    private fun loadStudios() {
        viewModelScope.launch {
            try {
                movieStudiosState.update {
                    getMovieStudiosUseCase.getStudios(movieId)
                }
            } catch (error: Exception) {
                error.rethrowCancellation {
                    Timber.w(error)
                }
            }
        }
    }

    @Suppress("UNCHECKED_CAST")
    val state: StateFlow<MovieDetailsState> = combine(
        movieState,
        movieRatingsState,
        movieStudiosState,
        movieProgressState,
        loadingState,
        loadingProgress,
        errorState,
    ) { state ->
        MovieDetailsState(
            movie = state[0] as Movie?,
            movieRatings = state[1] as ExternalRating?,
            movieStudios = state[2] as ImmutableList<String>?,
            movieProgress = state[3] as MovieDetailsState.ProgressState?,
            loading = state[4] as LoadingState,
            loadingProgress = state[5] as LoadingState,
            error = state[6] as? Exception,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = initialState,
    )
}
