package tv.trakt.trakt.core.lists.sections.personal.features.context.movie

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
import tv.trakt.trakt.analytics.Analytics
import tv.trakt.trakt.analytics.crashlytics.recordError
import tv.trakt.trakt.common.auth.session.SessionManager
import tv.trakt.trakt.common.helpers.LoadingState
import tv.trakt.trakt.common.helpers.LoadingState.DONE
import tv.trakt.trakt.common.helpers.LoadingState.IDLE
import tv.trakt.trakt.common.helpers.LoadingState.LOADING
import tv.trakt.trakt.common.helpers.extensions.nowUtcInstant
import tv.trakt.trakt.common.helpers.extensions.rethrowCancellation
import tv.trakt.trakt.common.model.CustomList
import tv.trakt.trakt.common.model.Movie
import tv.trakt.trakt.core.lists.sections.personal.usecases.manage.RemovePersonalListItemUseCase
import tv.trakt.trakt.core.lists.sections.watchlist.model.WatchlistItem
import tv.trakt.trakt.core.sync.usecases.UpdateMovieHistoryUseCase
import tv.trakt.trakt.core.sync.usecases.UpdateMovieWatchlistUseCase
import tv.trakt.trakt.core.user.data.local.UserProgressLocalDataSource
import tv.trakt.trakt.core.user.data.local.UserWatchlistLocalDataSource
import tv.trakt.trakt.core.user.usecases.lists.LoadUserWatchlistUseCase
import tv.trakt.trakt.core.user.usecases.progress.LoadUserProgressUseCase
import tv.trakt.trakt.ui.components.dateselection.DateSelectionResult

internal class ListMovieContextViewModel(
    private val movie: Movie,
    private val list: CustomList,
    private val updateMovieWatchlistUseCase: UpdateMovieWatchlistUseCase,
    private val updateMovieHistoryUseCase: UpdateMovieHistoryUseCase,
    private val removeListItemUseCase: RemovePersonalListItemUseCase,
    private val userProgressLocalSource: UserProgressLocalDataSource,
    private val userWatchlistLocalSource: UserWatchlistLocalDataSource,
    private val loadProgressUseCase: LoadUserProgressUseCase,
    private val loadWatchlistUseCase: LoadUserWatchlistUseCase,
    private val sessionManager: SessionManager,
    private val analytics: Analytics,
) : ViewModel() {
    private val initialState = ListMovieContextState()

    private val isWatchlistState = MutableStateFlow(initialState.isWatchlist)
    private val isWatchedState = MutableStateFlow(initialState.isWatched)

    private val loadingWatchedState = MutableStateFlow(initialState.loadingWatched)
    private val loadingWatchlistState = MutableStateFlow(initialState.loadingWatchlist)
    private val loadingListState = MutableStateFlow(initialState.loadingList)

    private val errorState = MutableStateFlow(initialState.error)

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            if (!sessionManager.isAuthenticated()) {
                return@launch
            }
            try {
                loadingWatchedState.update { LOADING }
                loadingWatchlistState.update { LOADING }
                loadingListState.update { LOADING }

                coroutineScope {
                    val watchlistAsync = async {
                        if (!userWatchlistLocalSource.isMoviesLoaded()) {
                            loadWatchlistUseCase.loadWatchlist()
                        }
                    }
                    val progressAsync = async {
                        if (!userProgressLocalSource.isMoviesLoaded()) {
                            loadProgressUseCase.loadMoviesProgress()
                        }
                    }

                    watchlistAsync.await()
                    progressAsync.await()

                    isWatchlistState.update {
                        userWatchlistLocalSource.containsMovie(movie.ids.trakt)
                    }
                    isWatchedState.update {
                        userProgressLocalSource.containsMovie(movie.ids.trakt)
                    }
                }
            } catch (error: Exception) {
                error.rethrowCancellation {
                    errorState.update { error }
                    Timber.recordError(error)
                }
            } finally {
                loadingWatchedState.update { IDLE }
                loadingWatchlistState.update { IDLE }
                loadingListState.update { IDLE }
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

                updateMovieWatchlistUseCase.addToWatchlist(
                    movieId = movie.ids.trakt,
                )
                userWatchlistLocalSource.addMovies(
                    movies = listOf(
                        WatchlistItem.MovieItem(
                            rank = 0,
                            movie = movie,
                            listedAt = nowUtcInstant(),
                        ),
                    ),
                    notify = true,
                )

                analytics.progress.logAddWatchlistMedia(
                    mediaType = "movie",
                    source = "list_movie_context",
                )
            } catch (error: Exception) {
                error.rethrowCancellation {
                    errorState.update { error }
                    Timber.recordError(error)
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

                updateMovieWatchlistUseCase.removeFromWatchlist(
                    movieId = movie.ids.trakt,
                )
                userWatchlistLocalSource.removeMovies(
                    ids = setOf(movie.ids.trakt),
                    notify = true,
                )

                analytics.progress.logRemoveWatchlistMedia(
                    mediaType = "movie",
                    source = "list_movie_context",
                )
            } catch (error: Exception) {
                error.rethrowCancellation {
                    errorState.update { error }
                    Timber.recordError(error)
                }
            } finally {
                loadingWatchlistState.update { DONE }
            }
        }
    }

    fun addToWatched(customDate: DateSelectionResult? = null) {
        if (isLoading()) {
            return
        }

        viewModelScope.launch {
            clear()
            try {
                loadingWatchedState.update { LOADING }

                updateMovieHistoryUseCase.addToWatched(
                    movieId = movie.ids.trakt,
                    customDate = customDate,
                )
                loadProgressUseCase.loadMoviesProgress()
                userWatchlistLocalSource.removeMovies(
                    ids = setOf(movie.ids.trakt),
                    notify = true,
                )

                analytics.progress.logAddWatchedMedia(
                    mediaType = "movie",
                    source = "list_movie_context",
                    date = customDate?.analyticsStrings,
                )
            } catch (error: Exception) {
                error.rethrowCancellation {
                    errorState.update { error }
                    Timber.recordError(error)
                }
            } finally {
                loadingWatchedState.update { DONE }
            }
        }
    }

    fun removeFromWatched() {
        if (isLoading()) {
            return
        }

        viewModelScope.launch {
            clear()
            try {
                loadingWatchedState.update { LOADING }

                updateMovieHistoryUseCase.removeAllFromHistory(movie.ids.trakt)
                userProgressLocalSource.removeMovies(setOf(movie.ids.trakt))

                analytics.progress.logRemoveWatchedMedia(
                    mediaType = "movie",
                    source = "list_movie_context",
                )
            } catch (error: Exception) {
                error.rethrowCancellation {
                    errorState.update { error }
                    Timber.recordError(error)
                }
            } finally {
                loadingWatchedState.update { DONE }
            }
        }
    }

    fun removeFromList() {
        if (isLoading()) {
            return
        }

        viewModelScope.launch {
            clear()
            try {
                loadingListState.update { LOADING }

                removeListItemUseCase.removeMovie(
                    listId = list.ids.trakt,
                    movieId = movie.ids.trakt,
                )
            } catch (error: Exception) {
                error.rethrowCancellation {
                    errorState.update { error }
                    Timber.recordError(error)
                }
            } finally {
                loadingListState.update { DONE }
            }
        }
    }

    fun clear() {
        loadingWatchedState.update { IDLE }
        loadingWatchlistState.update { IDLE }
        loadingListState.update { IDLE }

        errorState.update { null }
    }

    private fun isLoading(): Boolean {
        return loadingWatchedState.value.isLoading ||
            loadingWatchlistState.value.isLoading ||
            loadingListState.value.isLoading
    }

    val state: StateFlow<ListMovieContextState> = combine(
        isWatchlistState,
        isWatchedState,
        loadingWatchedState,
        loadingWatchlistState,
        loadingListState,
        errorState,
    ) { state ->
        ListMovieContextState(
            isWatchlist = state[0] as Boolean,
            isWatched = state[1] as Boolean,
            loadingWatched = state[2] as LoadingState,
            loadingWatchlist = state[3] as LoadingState,
            loadingList = state[4] as LoadingState,
            error = state[5] as Exception?,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = initialState,
    )
}
