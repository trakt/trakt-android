package tv.trakt.trakt.core.movies.ui.context

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
import tv.trakt.trakt.common.auth.session.SessionManager
import tv.trakt.trakt.common.core.sync.changes.DataChange.Kind
import tv.trakt.trakt.common.core.sync.changes.DataChanges
import tv.trakt.trakt.common.core.user.usecases.lists.LoadUserWatchlistUseCase
import tv.trakt.trakt.common.core.user.usecases.progress.LoadUserProgressUseCase
import tv.trakt.trakt.common.data.local.UserProgressLocalSource
import tv.trakt.trakt.common.data.local.UserWatchlistLocalSource
import tv.trakt.trakt.common.data.local.UserWatchlistMinimalLocalSource
import tv.trakt.trakt.common.firebase.analytics.Analytics
import tv.trakt.trakt.common.helpers.LoadingState
import tv.trakt.trakt.common.helpers.LoadingState.Done
import tv.trakt.trakt.common.helpers.LoadingState.Idle
import tv.trakt.trakt.common.helpers.LoadingState.Loading
import tv.trakt.trakt.common.helpers.errors.GlobalErrorsManager
import tv.trakt.trakt.common.helpers.extensions.recordError
import tv.trakt.trakt.common.helpers.extensions.rethrowCancellation
import tv.trakt.trakt.common.model.DateSelectionResult
import tv.trakt.trakt.common.model.Movie
import tv.trakt.trakt.common.model.User
import tv.trakt.trakt.common.model.lists.CustomList
import tv.trakt.trakt.core.checkin.data.CheckInManager
import tv.trakt.trakt.core.lists.sections.personal.usecases.manage.RemovePersonalListItemUseCase
import tv.trakt.trakt.core.ratings.rateprompt.RatePromptManager
import tv.trakt.trakt.core.sync.usecases.UpdateMovieHistoryUseCase
import tv.trakt.trakt.core.sync.usecases.UpdateMovieWatchlistUseCase

internal class MovieContextViewModel(
    private val appContext: Context,
    private val movie: Movie,
    private val list: CustomList?,
    private val updateMovieWatchlistUseCase: UpdateMovieWatchlistUseCase,
    private val updateMovieHistoryUseCase: UpdateMovieHistoryUseCase,
    private val removeListItemUseCase: RemovePersonalListItemUseCase,
    private val userProgressLocalSource: UserProgressLocalSource,
    private val userWatchlistLocalSource: UserWatchlistLocalSource,
    private val userWatchlistMinLocalSource: UserWatchlistMinimalLocalSource,
    private val loadProgressUseCase: LoadUserProgressUseCase,
    private val loadWatchlistUseCase: LoadUserWatchlistUseCase,
    private val dataChanges: DataChanges,
    private val sessionManager: SessionManager,
    private val checkInManager: CheckInManager,
    private val ratePromptManager: RatePromptManager,
    private val errorsManager: GlobalErrorsManager,
    private val analytics: Analytics,
) : ViewModel() {
    private val initialState = MovieContextState()
    private val analyticsSource = if (list != null) "list_movie_context" else "movie_context"

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
                    val watchlistAsync = async {
                        if (!userWatchlistMinLocalSource.isMoviesLoaded()) {
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

                updateMovieWatchlistUseCase.addToWatchlist(movie)

                analytics.progress.logAddWatchlistMedia(
                    mediaType = "movie",
                    source = analyticsSource,
                )

                loadingWatchlistState.update { Done }
            } catch (error: Exception) {
                error.rethrowCancellation {
                    if (!errorsManager.tryEmitVipLimit(error)) {
                        errorState.update { error }
                        Timber.recordError(error)
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

                updateMovieWatchlistUseCase.removeFromWatchlist(movieId = movie.ids.trakt)

                analytics.progress.logRemoveWatchlistMedia(
                    mediaType = "movie",
                    source = analyticsSource,
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
                ratePromptManager.checkRecentlyWatched()

                analytics.progress.logAddWatchedMedia(
                    mediaType = "movie",
                    source = analyticsSource,
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
                    context = appContext,
                )
                userWatchlistLocalSource.removeMovies(
                    ids = setOf(movie.ids.trakt),
                )
                userWatchlistMinLocalSource.removeMovies(
                    ids = setOf(movie.ids.trakt),
                )

                dataChanges.notify(Kind.Watchlist)

                analytics.progress.logAddWatchedMedia(
                    mediaType = "movie",
                    source = analyticsSource,
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

    fun removeFromWatched() {
        if (isLoading()) {
            return
        }

        viewModelScope.launch {
            clear()
            try {
                loadingWatchedState.update { Loading }

                updateMovieHistoryUseCase.removeAllFromHistory(movie.ids.trakt)

                analytics.progress.logRemoveWatchedMedia(
                    mediaType = "movie",
                    source = analyticsSource,
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

    fun removeFromList() {
        val list = list ?: return
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
                analytics.lists.logListItemRemove()
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

    val state: StateFlow<MovieContextState> = combine(
        isWatchlistState,
        isWatchedState,
        loadingWatchedState,
        loadingWatchlistState,
        loadingCheckInState,
        sessionManager.observeProfile(),
        errorState,
        loadingListState,
    ) { state ->
        MovieContextState(
            isWatchlist = state[0] as Boolean,
            isWatched = state[1] as Boolean,
            loadingWatched = state[2] as LoadingState,
            loadingWatchlist = state[3] as LoadingState,
            loadingCheckIn = state[4] as LoadingState,
            user = state[5] as User?,
            error = state[6] as Exception?,
            loadingList = state[7] as LoadingState,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = initialState,
    )
}
