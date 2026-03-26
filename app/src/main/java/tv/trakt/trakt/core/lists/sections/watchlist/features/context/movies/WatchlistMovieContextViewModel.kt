package tv.trakt.trakt.core.lists.sections.watchlist.features.context.movies

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import tv.trakt.trakt.analytics.Analytics
import tv.trakt.trakt.analytics.crashlytics.recordError
import tv.trakt.trakt.common.auth.session.SessionManager
import tv.trakt.trakt.common.helpers.LoadingState
import tv.trakt.trakt.common.helpers.LoadingState.Done
import tv.trakt.trakt.common.helpers.LoadingState.Idle
import tv.trakt.trakt.common.helpers.LoadingState.Loading
import tv.trakt.trakt.common.helpers.extensions.rethrowCancellation
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.model.User
import tv.trakt.trakt.core.checkin.data.CheckInManager
import tv.trakt.trakt.core.checkin.data.updates.CheckInUpdates
import tv.trakt.trakt.core.sync.usecases.UpdateMovieHistoryUseCase
import tv.trakt.trakt.core.sync.usecases.UpdateMovieWatchlistUseCase
import tv.trakt.trakt.core.user.data.local.watchlist.UserWatchlistLocalDataSource
import tv.trakt.trakt.core.user.data.local.watchlist.WatchlistUpdates
import tv.trakt.trakt.core.user.data.local.watchlist.WatchlistUpdates.Source.Default
import tv.trakt.trakt.core.user.data.local.watchlist.minimal.UserWatchlistMinimalLocalDataSource
import tv.trakt.trakt.core.user.usecases.progress.LoadUserProgressUseCase
import tv.trakt.trakt.ui.components.dateselection.DateSelectionResult

internal class WatchlistMovieContextViewModel(
    private val appContext: Context,
    private val updateMovieHistoryUseCase: UpdateMovieHistoryUseCase,
    private val updateMovieWatchlistUseCase: UpdateMovieWatchlistUseCase,
    private val userWatchlistLocalSource: UserWatchlistLocalDataSource,
    private val userWatchlistMinLocalSource: UserWatchlistMinimalLocalDataSource,
    private val loadProgressUseCase: LoadUserProgressUseCase,
    private val sessionManager: SessionManager,
    private val checkInManager: CheckInManager,
    private val watchlistUpdates: WatchlistUpdates,
    private val analytics: Analytics,
) : ViewModel() {
    private val initialState = WatchlistMovieContextState()

    private val loadingWatchedState = MutableStateFlow(initialState.loadingWatched)
    private val loadingWatchlistState = MutableStateFlow(initialState.loadingWatchlist)
    private val loadingCheckInState = MutableStateFlow(initialState.loadingCheckIn)

    private val userState = MutableStateFlow(initialState.user)
    private val errorState = MutableStateFlow(initialState.error)

    init {
        loadUser()
    }

    private fun loadUser() {
        viewModelScope.launch {
            userState.update {
                sessionManager.getProfile()
            }
        }
    }

    fun addToWatched(
        movieId: TraktId,
        customDate: DateSelectionResult? = null,
    ) {
        if (isLoading()) {
            return
        }

        viewModelScope.launch {
            clear()
            try {
                loadingWatchedState.update { Loading }

                updateMovieHistoryUseCase.addToWatched(
                    movieId = movieId,
                    customDate = customDate,
                )
                userWatchlistLocalSource.removeMovies(setOf(movieId))
                userWatchlistMinLocalSource.removeMovies(setOf(movieId))

                loadProgressUseCase.loadMoviesProgress()

                analytics.progress.logAddWatchedMedia(
                    mediaType = "movie",
                    source = "watchlist_movie_context",
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

    fun removeFromWatchlist(movieId: TraktId) {
        if (isLoading()) return
        viewModelScope.launch {
            clear()
            try {
                loadingWatchlistState.update { Loading }

                updateMovieWatchlistUseCase.removeFromWatchlist(movieId = movieId)
                userWatchlistLocalSource.removeMovies(setOf(movieId))
                userWatchlistMinLocalSource.removeMovies(setOf(movieId))
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

    fun addToCheckIn(movieId: TraktId) {
        if (isLoading()) return
        viewModelScope.launch {
            clear()
            try {
                loadingCheckInState.update { Loading }

                checkInManager.startMovie(
                    movieId = movieId,
                    source = CheckInUpdates.Source.Watchlist,
                    context = appContext,
                )
                userWatchlistLocalSource.removeMovies(
                    ids = setOf(movieId),
                )
                userWatchlistMinLocalSource.removeMovies(
                    ids = setOf(movieId),
                )

                watchlistUpdates.notifyUpdate(Default)

                analytics.progress.logAddWatchedMedia(
                    mediaType = "movie",
                    source = "watchlist_movie_context",
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

    fun clear() {
        loadingWatchedState.update { Idle }
        loadingWatchlistState.update { Idle }
        loadingCheckInState.update { Idle }
        errorState.update { null }
    }

    private fun isLoading(): Boolean {
        return loadingWatchedState.value.isLoading ||
            loadingWatchlistState.value.isLoading ||
            loadingCheckInState.value.isLoading
    }

    val state = combine(
        loadingWatchedState,
        loadingWatchlistState,
        loadingCheckInState,
        userState,
        errorState,
    ) { state ->
        WatchlistMovieContextState(
            loadingWatched = state[0] as LoadingState,
            loadingWatchlist = state[1] as LoadingState,
            loadingCheckIn = state[2] as LoadingState,
            user = state[3] as User?,
            error = state[4] as Exception?,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = initialState,
    )
}
