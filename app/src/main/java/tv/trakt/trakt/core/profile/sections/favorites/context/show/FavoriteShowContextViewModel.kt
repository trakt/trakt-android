package tv.trakt.trakt.core.profile.sections.favorites.context.show

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
import tv.trakt.trakt.common.model.Show
import tv.trakt.trakt.common.model.User
import tv.trakt.trakt.core.sync.usecases.UpdateShowFavoritesUseCase
import tv.trakt.trakt.core.sync.usecases.UpdateShowHistoryUseCase

private const val ANALYTICS_SOURCE = "favorite_show_context"

@Suppress("UNCHECKED_CAST")
internal class FavoriteShowContextViewModel(
    private val show: Show,
    private val sessionManager: SessionManager,
    private val updateShowFavoritesUseCase: UpdateShowFavoritesUseCase,
    private val updateShowHistoryUseCase: UpdateShowHistoryUseCase,
    private val loadProgressUseCase: LoadUserProgressUseCase,
    private val userFavoritesLocalSource: UserFavoritesLocalSource,
    private val userProgressLocalSource: UserProgressLocalSource,
    private val userWatchlistLocalSource: UserWatchlistLocalSource,
    private val userWatchlistMinLocalSource: UserWatchlistMinimalLocalSource,
    private val dataChanges: DataChanges,
    private val analytics: Analytics,
) : ViewModel() {
    private val initialState = FavoriteShowContextState()

    private val isWatchedState = MutableStateFlow(initialState.isWatched)
    private val loadingFavoriteState = MutableStateFlow(initialState.loadingFavorite)
    private val loadingWatchedState = MutableStateFlow(initialState.loadingWatched)
    private val userState = MutableStateFlow(initialState.user)
    private val errorState = MutableStateFlow(initialState.error)

    init {
        loadUser()
        loadData()
    }

    private fun loadUser() {
        viewModelScope.launch {
            userState.update {
                sessionManager.getProfile()
            }
        }
    }

    private fun loadData() {
        viewModelScope.launch {
            if (!sessionManager.isAuthenticated()) {
                return@launch
            }
            try {
                loadingWatchedState.update { Loading }

                if (!userProgressLocalSource.isShowsLoaded()) {
                    loadProgressUseCase.loadShowsProgress()
                }
                isWatchedState.update {
                    userProgressLocalSource.getShows(setOf(show.ids.trakt))
                        .firstOrNull()
                        ?.isCompleted(show) == true
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

                updateShowFavoritesUseCase.removeFromFavorites(show.ids.trakt)
                userFavoritesLocalSource.removeShows(setOf(show.ids.trakt))
                dataChanges.notify(Kind.Favorites)

                analytics.ratings.logFavoriteRemove(
                    mediaType = MediaType.Show.value,
                    source = "show_context",
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

                updateShowHistoryUseCase.addToWatched(
                    showId = show.ids.trakt,
                    customDate = customDate,
                )
                loadProgressUseCase.loadShowsProgress()
                userWatchlistLocalSource.removeShows(ids = setOf(show.ids.trakt))
                userWatchlistMinLocalSource.removeShows(ids = setOf(show.ids.trakt))

                dataChanges.notify(Kind.History, Kind.Watchlist)

                analytics.progress.logAddWatchedMedia(
                    mediaType = MediaType.Show.value,
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

                updateShowHistoryUseCase.removeAllFromHistory(show.ids.trakt)
                userProgressLocalSource.removeShows(ids = setOf(show.ids.trakt))
                dataChanges.notify(Kind.History, Kind.Progress)

                analytics.progress.logRemoveWatchedMedia(
                    mediaType = MediaType.Show.value,
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
        userState,
        errorState,
    ) { state ->
        FavoriteShowContextState(
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
