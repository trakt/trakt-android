package tv.trakt.trakt.core.profile.sections.favorites.context.movie

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import tv.trakt.trakt.common.auth.session.SessionManager
import tv.trakt.trakt.common.core.sync.changes.DataChange.Kind
import tv.trakt.trakt.common.core.sync.changes.DataChanges
import tv.trakt.trakt.common.core.user.usecases.progress.LoadUserProgressUseCase
import tv.trakt.trakt.common.data.local.UserFavoritesLocalSource
import tv.trakt.trakt.common.data.local.UserProgressLocalSource
import tv.trakt.trakt.common.data.local.UserWatchlistLocalSource
import tv.trakt.trakt.common.data.local.UserWatchlistMinimalLocalSource
import tv.trakt.trakt.common.firebase.analytics.Analytics
import tv.trakt.trakt.common.helpers.LoadingState
import tv.trakt.trakt.common.helpers.LoadingState.Done
import tv.trakt.trakt.common.helpers.LoadingState.Idle
import tv.trakt.trakt.common.helpers.LoadingState.Loading
import tv.trakt.trakt.common.helpers.extensions.recordError
import tv.trakt.trakt.common.helpers.extensions.rethrowCancellation
import tv.trakt.trakt.common.model.DateSelectionResult
import tv.trakt.trakt.common.model.MediaType
import tv.trakt.trakt.common.model.Movie
import tv.trakt.trakt.common.model.User
import tv.trakt.trakt.core.ratings.rateprompt.RatePromptManager
import tv.trakt.trakt.core.sync.usecases.UpdateMovieFavoritesUseCase
import tv.trakt.trakt.core.sync.usecases.UpdateMovieHistoryUseCase

private const val ANALYTICS_SOURCE = "favorite_movie_context"

@Suppress("UNCHECKED_CAST")
internal class FavoriteMovieContextViewModel(
    private val movie: Movie,
    private val sessionManager: SessionManager,
    private val updateMovieFavoritesUseCase: UpdateMovieFavoritesUseCase,
    private val updateMovieHistoryUseCase: UpdateMovieHistoryUseCase,
    private val loadProgressUseCase: LoadUserProgressUseCase,
    private val userProgressLocalSource: UserProgressLocalSource,
    private val ratePromptManager: RatePromptManager,
    private val analytics: Analytics,
) : ViewModel() {
    private val initialState = FavoriteMovieContextState()

    private val isWatchedState = MutableStateFlow(initialState.isWatched)
    private val loadingFavoriteState = MutableStateFlow(initialState.loadingFavorite)
    private val loadingWatchedState = MutableStateFlow(initialState.loadingWatched)
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

                if (!userProgressLocalSource.isMoviesLoaded()) {
                    loadProgressUseCase.loadMoviesProgress()
                }
                isWatchedState.update {
                    userProgressLocalSource.containsMovie(movie.ids.trakt)
                }
            } catch (error: Exception) {
                error.rethrowCancellation {
                    errorState.update { error }
                    Timber.recordError(error)
                }
            } finally {
                loadingWatchedState.update { Idle }
            }
        }
    }

    fun removeFromFavorites() {
        if (isLoading()) {
            return
        }

        viewModelScope.launch {
            if (!sessionManager.isAuthenticated()) {
                return@launch
            }
            try {
                loadingFavoriteState.update { Loading }

                updateMovieFavoritesUseCase.removeFromFavorites(movie.ids.trakt)

                analytics.ratings.logFavoriteRemove(
                    mediaType = MediaType.Movie.value,
                    source = "movie_context",
                )

                loadingFavoriteState.update { Done }
            } catch (error: Exception) {
                error.rethrowCancellation {
                    errorState.update { error }
                    Timber.recordError(error)
                }
            }
        }
    }

    fun addToWatched(customDate: DateSelectionResult? = null) {
        if (isLoading()) {
            return
        }

        viewModelScope.launch {
            try {
                loadingWatchedState.update { Loading }

                updateMovieHistoryUseCase.addToWatched(
                    movieId = movie.ids.trakt,
                    customDate = customDate,
                )
                ratePromptManager.checkRecentlyWatched()

                analytics.progress.logAddWatchedMedia(
                    mediaType = MediaType.Movie.value,
                    source = ANALYTICS_SOURCE,
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
            try {
                loadingWatchedState.update { Loading }

                updateMovieHistoryUseCase.removeAllFromHistory(movie.ids.trakt)

                analytics.progress.logRemoveWatchedMedia(
                    mediaType = MediaType.Movie.value,
                    source = ANALYTICS_SOURCE,
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

    private fun isLoading(): Boolean {
        return loadingFavoriteState.value.isLoading ||
            loadingWatchedState.value.isLoading
    }

    val state = combine(
        isWatchedState,
        loadingFavoriteState,
        loadingWatchedState,
        sessionManager.observeProfile(),
        errorState,
    ) { state ->
        FavoriteMovieContextState(
            isWatched = state[0] as Boolean,
            loadingFavorite = state[1] as LoadingState,
            loadingWatched = state[2] as LoadingState,
            user = state[3] as User?,
            error = state[4] as Exception?,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = initialState,
    )
}
