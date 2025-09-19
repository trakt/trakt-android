package tv.trakt.trakt.core.movies.ui.context

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
import tv.trakt.trakt.common.helpers.LoadingState.IDLE
import tv.trakt.trakt.common.helpers.LoadingState.LOADING
import tv.trakt.trakt.common.helpers.extensions.nowUtcInstant
import tv.trakt.trakt.common.helpers.extensions.rethrowCancellation
import tv.trakt.trakt.common.model.Movie
import tv.trakt.trakt.core.lists.sections.watchlist.model.WatchlistItem
import tv.trakt.trakt.core.sync.usecases.UpdateMovieHistoryUseCase
import tv.trakt.trakt.core.sync.usecases.UpdateMovieWatchlistUseCase
import tv.trakt.trakt.core.user.data.local.UserWatchlistLocalDataSource
import tv.trakt.trakt.core.user.usecase.watchlist.LoadUserWatchlistUseCase

internal class MovieContextViewModel(
    private val movie: Movie,
    private val updateMovieWatchlistUseCase: UpdateMovieWatchlistUseCase,
    private val updateMovieHistoryUseCase: UpdateMovieHistoryUseCase,
    private val userWatchlistLocalSource: UserWatchlistLocalDataSource,
    private val loadWatchlistUseCase: LoadUserWatchlistUseCase,
) : ViewModel() {
    private val initialState = MovieContextState()

    private val isWatchlistState = MutableStateFlow(initialState.isWatchlist)
    private val isWatchedState = MutableStateFlow(initialState.isWatched)

    private val loadingWatchedState = MutableStateFlow(initialState.loadingWatched)
    private val loadingWatchlistState = MutableStateFlow(initialState.loadingWatchlist)

    private val errorState = MutableStateFlow(initialState.error)

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            try {
                loadingWatchedState.update { LOADING }
                loadingWatchlistState.update { LOADING }

                if (!userWatchlistLocalSource.isMoviesLoaded()) {
                    loadWatchlistUseCase.loadWatchlist()
                }

                isWatchlistState.update {
                    userWatchlistLocalSource.containsMovie(movie.ids.trakt)
                }
            } catch (error: Exception) {
                error.rethrowCancellation {
                    errorState.update { error }
                    Timber.w(error)
                }
            } finally {
                loadingWatchedState.update { IDLE }
                loadingWatchlistState.update { IDLE }
            }
        }
    }

    fun addToWatchlist() {
        if (isLoading()) {
            return
        }

        viewModelScope.launch {
            clear()
            try {
                loadingWatchlistState.update { LOADING }

                updateMovieWatchlistUseCase.addToWatchlist(movieId = movie.ids.trakt)
                userWatchlistLocalSource.addMovies(
                    listOf(
                        WatchlistItem.MovieItem(
                            rank = 0,
                            movie = movie,
                            listedAt = nowUtcInstant(),
                        ),
                    ),
                )

                isWatchlistState.update { true }
            } catch (error: Exception) {
                error.rethrowCancellation {
                    errorState.update { error }
                    Timber.w(error)
                }
            } finally {
                loadingWatchlistState.update { DONE }
            }
        }
    }

    fun removeFromWatchlist() {
        if (isLoading()) {
            return
        }

        viewModelScope.launch {
            clear()
            try {
                loadingWatchlistState.update { LOADING }

                updateMovieWatchlistUseCase.removeFromWatchlist(movieId = movie.ids.trakt)
                userWatchlistLocalSource.removeMovies(setOf(movie.ids.trakt))

                isWatchlistState.update { false }
            } catch (error: Exception) {
                error.rethrowCancellation {
                    errorState.update { error }
                    Timber.w(error)
                }
            } finally {
                loadingWatchlistState.update { DONE }
            }
        }
    }

    fun addToWatched() {
        if (isLoading()) {
            return
        }

        viewModelScope.launch {
            clear()
            try {
                loadingWatchedState.update { LOADING }

                updateMovieHistoryUseCase.addToWatched(movie.ids.trakt)
                userWatchlistLocalSource.removeMovies(setOf(movie.ids.trakt))

                isWatchedState.update { true }
                isWatchlistState.update { false }
            } catch (error: Exception) {
                error.rethrowCancellation {
                    errorState.update { error }
                    Timber.w(error)
                }
            } finally {
                loadingWatchedState.update { DONE }
            }
        }
    }

    fun clear() {
        loadingWatchedState.update { IDLE }
        loadingWatchlistState.update { IDLE }

        errorState.update { null }
    }

    private fun isLoading(): Boolean {
        return loadingWatchedState.value.isLoading || loadingWatchlistState.value.isLoading
    }

    val state: StateFlow<MovieContextState> = combine(
        isWatchlistState,
        isWatchedState,
        loadingWatchedState,
        loadingWatchlistState,
        errorState,
    ) { s1, s2, s3, s4, s5 ->
        MovieContextState(
            isWatchlist = s1,
            isWatched = s2,
            loadingWatched = s3,
            loadingWatchlist = s4,
            error = s5,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = initialState,
    )
}
