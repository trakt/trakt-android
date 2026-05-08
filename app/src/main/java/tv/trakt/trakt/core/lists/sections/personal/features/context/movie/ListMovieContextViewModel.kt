package tv.trakt.trakt.core.lists.sections.personal.features.context.movie

import android.content.Context
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
import tv.trakt.trakt.analytics.crashlytics.recordError
import tv.trakt.trakt.common.auth.session.SessionManager
import tv.trakt.trakt.common.firebase.analytics.Analytics
import tv.trakt.trakt.common.helpers.LoadingState
import tv.trakt.trakt.common.helpers.LoadingState.Done
import tv.trakt.trakt.common.helpers.LoadingState.Idle
import tv.trakt.trakt.common.helpers.LoadingState.Loading
import tv.trakt.trakt.common.helpers.errors.GlobalErrorListener
import tv.trakt.trakt.common.helpers.extensions.HTTP_ERROR_TRAKT_VIP_LIMIT
import tv.trakt.trakt.common.helpers.extensions.getHttpCode
import tv.trakt.trakt.common.helpers.extensions.nowUtcInstant
import tv.trakt.trakt.common.helpers.extensions.rethrowCancellation
import tv.trakt.trakt.common.model.CustomList
import tv.trakt.trakt.common.model.Movie
import tv.trakt.trakt.core.checkin.data.CheckInManager
import tv.trakt.trakt.core.checkin.data.updates.CheckInUpdates.Source.MovieContext
import tv.trakt.trakt.core.lists.sections.personal.usecases.manage.RemovePersonalListItemUseCase
import tv.trakt.trakt.core.lists.sections.watchlist.model.WatchlistItem
import tv.trakt.trakt.core.ratings.rateprompt.RatePromptManager
import tv.trakt.trakt.core.sync.usecases.UpdateMovieHistoryUseCase
import tv.trakt.trakt.core.sync.usecases.UpdateMovieWatchlistUseCase
import tv.trakt.trakt.core.user.data.local.UserProgressLocalDataSource
import tv.trakt.trakt.core.user.data.local.watchlist.UserWatchlistLocalDataSource
import tv.trakt.trakt.core.user.data.local.watchlist.WatchlistUpdates
import tv.trakt.trakt.core.user.data.local.watchlist.WatchlistUpdates.Source
import tv.trakt.trakt.core.user.data.local.watchlist.minimal.UserWatchlistMinimalLocalDataSource
import tv.trakt.trakt.core.user.usecases.lists.LoadUserWatchlistUseCase
import tv.trakt.trakt.core.user.usecases.progress.LoadUserProgressUseCase
import tv.trakt.trakt.ui.components.dateselection.DateSelectionResult

internal class ListMovieContextViewModel(
    private val appContext: Context,
    private val movie: Movie,
    private val list: CustomList,
    private val updateMovieWatchlistUseCase: UpdateMovieWatchlistUseCase,
    private val updateMovieHistoryUseCase: UpdateMovieHistoryUseCase,
    private val removeListItemUseCase: RemovePersonalListItemUseCase,
    private val userProgressLocalSource: UserProgressLocalDataSource,
    private val userWatchlistLocalSource: UserWatchlistLocalDataSource,
    private val userWatchlistMinLocalSource: UserWatchlistMinimalLocalDataSource,
    private val loadProgressUseCase: LoadUserProgressUseCase,
    private val loadWatchlistMinUseCase: LoadUserWatchlistUseCase,
    private val watchlistUpdates: WatchlistUpdates,
    private val sessionManager: SessionManager,
    private val checkInManager: CheckInManager,
    private val ratePromptManager: RatePromptManager,
    private val globalErrors: GlobalErrorListener,
    private val analytics: Analytics,
) : ViewModel() {
    private val initialState = ListMovieContextState()

    private val isWatchlistState = MutableStateFlow(initialState.isWatchlist)
    private val isWatchedState = MutableStateFlow(initialState.isWatched)

    private val loadingWatchedState = MutableStateFlow(initialState.loadingWatched)
    private val loadingWatchlistState = MutableStateFlow(initialState.loadingWatchlist)
    private val loadingCheckInState = MutableStateFlow(initialState.loadingCheckIn)
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
                loadingWatchedState.update { Loading }
                loadingWatchlistState.update { Loading }
                loadingListState.update { Loading }

                coroutineScope {
                    val watchlistMinAsync = async {
                        if (!loadWatchlistMinUseCase.isMoviesLoaded()) {
                            loadWatchlistMinUseCase.loadWatchlist()
                        }
                    }

                    val progressAsync = async {
                        if (!userProgressLocalSource.isMoviesLoaded()) {
                            loadProgressUseCase.loadMoviesProgress()
                        }
                    }

                    watchlistMinAsync.await()
                    progressAsync.await()

                    isWatchlistState.update {
                        userWatchlistMinLocalSource.containsMovie(movie.ids.trakt)
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
                loadingWatchedState.update { Idle }
                loadingWatchlistState.update { Idle }
                loadingListState.update { Idle }
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
                loadingWatchlistState.update { Loading }

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
                )
                userWatchlistMinLocalSource.addMovies(
                    movies = setOf(movie.ids.trakt),
                )

                watchlistUpdates.notifyUpdate(Source.Default)

                analytics.progress.logAddWatchlistMedia(
                    mediaType = "movie",
                    source = "list_movie_context",
                )
                loadingWatchlistState.update { Done }
            } catch (error: Exception) {
                error.rethrowCancellation {
                    when (error.getHttpCode()) {
                        HTTP_ERROR_TRAKT_VIP_LIMIT -> {
                            globalErrors.tryEmit(error)
                        }
                        else -> {
                            errorState.update { error }
                            Timber.recordError(error)
                        }
                    }
                    loadingWatchlistState.update { Idle }
                }
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
                loadingWatchlistState.update { Loading }

                updateMovieWatchlistUseCase.removeFromWatchlist(
                    movieId = movie.ids.trakt,
                )

                userWatchlistLocalSource.removeMovies(
                    ids = setOf(movie.ids.trakt),
                )
                userWatchlistMinLocalSource.removeMovies(
                    ids = setOf(movie.ids.trakt),
                )
                watchlistUpdates.notifyUpdate(Source.Default)

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
                loadingWatchlistState.update { Done }
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
                loadingWatchedState.update { Loading }

                updateMovieHistoryUseCase.addToWatched(
                    movieId = movie.ids.trakt,
                    customDate = customDate,
                )
                loadProgressUseCase.loadMoviesProgress()

                userWatchlistLocalSource.removeMovies(
                    ids = setOf(movie.ids.trakt),
                )
                userWatchlistMinLocalSource.removeMovies(
                    ids = setOf(movie.ids.trakt),
                )
                watchlistUpdates.notifyUpdate(Source.Default)
                ratePromptManager.checkMovies()

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
                loadingWatchedState.update { Done }
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
                loadingWatchedState.update { Loading }

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
                loadingWatchedState.update { Done }
            }
        }
    }

    fun addToCheckIn() {
        if (isLoading()) {
            return
        }

        viewModelScope.launch {
            clear()
            try {
                loadingCheckInState.update { Loading }

                checkInManager.startMovie(
                    movieId = movie.ids.trakt,
                    source = MovieContext,
                    context = appContext,
                )
                userWatchlistLocalSource.removeMovies(
                    ids = setOf(movie.ids.trakt),
                )
                userWatchlistMinLocalSource.removeMovies(
                    ids = setOf(movie.ids.trakt),
                )
                watchlistUpdates.notifyUpdate(Source.Default)

                analytics.progress.logAddWatchedMedia(
                    mediaType = "movie",
                    source = "list_movie_context",
                    date = "checkin",
                )
            } catch (error: Exception) {
                error.rethrowCancellation {
                    errorState.update { error }
                    Timber.recordError(error)
                }
            } finally {
                loadingCheckInState.update { Done }
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
                loadingListState.update { Loading }

                removeListItemUseCase.removeMovie(
                    listId = list.ids.trakt,
                    ownerId = list.user.ids.trakt,
                    movieId = movie.ids.trakt,
                )
            } catch (error: Exception) {
                error.rethrowCancellation {
                    errorState.update { error }
                    Timber.recordError(error)
                }
            } finally {
                loadingListState.update { Done }
            }
        }
    }

    fun clear() {
        loadingWatchedState.update { Idle }
        loadingWatchlistState.update { Idle }
        loadingCheckInState.update { Idle }
        loadingListState.update { Idle }

        errorState.update { null }
    }

    private fun isLoading(): Boolean {
        return loadingWatchedState.value.isLoading ||
            loadingWatchlistState.value.isLoading ||
            loadingCheckInState.value.isLoading ||
            loadingListState.value.isLoading
    }

    val state: StateFlow<ListMovieContextState> = combine(
        isWatchlistState,
        isWatchedState,
        loadingWatchedState,
        loadingWatchlistState,
        loadingCheckInState,
        loadingListState,
        errorState,
    ) { state ->
        ListMovieContextState(
            isWatchlist = state[0] as Boolean,
            isWatched = state[1] as Boolean,
            loadingWatched = state[2] as LoadingState,
            loadingWatchlist = state[3] as LoadingState,
            loadingCheckIn = state[4] as LoadingState,
            loadingList = state[5] as LoadingState,
            error = state[6] as Exception?,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = initialState,
    )
}
