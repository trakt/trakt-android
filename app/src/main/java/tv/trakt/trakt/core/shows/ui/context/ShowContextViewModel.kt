package tv.trakt.trakt.core.shows.ui.context

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
import tv.trakt.trakt.common.core.lists.model.WatchlistItem
import tv.trakt.trakt.common.core.user.data.local.UserProgressLocalDataSource
import tv.trakt.trakt.common.core.user.data.local.watchlist.UserWatchlistLocalDataSource
import tv.trakt.trakt.common.core.user.data.local.watchlist.WatchlistUpdates
import tv.trakt.trakt.common.core.user.data.local.watchlist.WatchlistUpdates.Source.Default
import tv.trakt.trakt.common.core.user.data.local.watchlist.minimal.UserWatchlistMinimalLocalDataSource
import tv.trakt.trakt.common.core.user.usecases.lists.LoadUserWatchlistUseCase
import tv.trakt.trakt.common.core.user.usecases.progress.LoadUserProgressUseCase
import tv.trakt.trakt.common.core.user.usecases.progress.updates.ProgressUpdates
import tv.trakt.trakt.common.firebase.analytics.Analytics
import tv.trakt.trakt.common.helpers.LoadingState
import tv.trakt.trakt.common.helpers.LoadingState.Done
import tv.trakt.trakt.common.helpers.LoadingState.Idle
import tv.trakt.trakt.common.helpers.LoadingState.Loading
import tv.trakt.trakt.common.helpers.errors.GlobalErrorsManager
import tv.trakt.trakt.common.helpers.extensions.HTTP_ERROR_TRAKT_VIP_LIMIT
import tv.trakt.trakt.common.helpers.extensions.getHttpCode
import tv.trakt.trakt.common.helpers.extensions.nowUtcInstant
import tv.trakt.trakt.common.helpers.extensions.recordError
import tv.trakt.trakt.common.helpers.extensions.rethrowCancellation
import tv.trakt.trakt.common.model.DateSelectionResult
import tv.trakt.trakt.common.model.Show
import tv.trakt.trakt.common.model.User
import tv.trakt.trakt.core.sync.usecases.UpdateShowHistoryUseCase
import tv.trakt.trakt.core.sync.usecases.UpdateShowWatchlistUseCase

internal class ShowContextViewModel(
    private val show: Show,
    private val updateWatchlistUseCase: UpdateShowWatchlistUseCase,
    private val updateHistoryUseCase: UpdateShowHistoryUseCase,
    private val userProgressLocalSource: UserProgressLocalDataSource,
    private val userWatchlistLocalSource: UserWatchlistLocalDataSource,
    private val userWatchlistMinLocalSource: UserWatchlistMinimalLocalDataSource,
    private val loadProgressUseCase: LoadUserProgressUseCase,
    private val loadWatchlistUseCase: LoadUserWatchlistUseCase,
    private val progressUpdates: ProgressUpdates,
    private val watchlistUpdates: WatchlistUpdates,
    private val sessionManager: SessionManager,
    private val errorsManager: GlobalErrorsManager,
    private val analytics: Analytics,
) : ViewModel() {
    private val initialState = ShowContextState()

    private val isWatchlistState = MutableStateFlow(initialState.isWatchlist)
    private val isWatchedState = MutableStateFlow(initialState.isWatched)

    private val loadingWatchedState = MutableStateFlow(initialState.loadingWatched)
    private val loadingWatchlistState = MutableStateFlow(initialState.loadingWatchlist)

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
                loadingWatchlistState.update { Loading }

                coroutineScope {
                    val watchlistAsync = async {
                        if (!userWatchlistMinLocalSource.isShowsLoaded()) {
                            loadWatchlistUseCase.loadWatchlist()
                        }
                    }
                    val progressAsync = async {
                        if (!userProgressLocalSource.isShowsLoaded()) {
                            loadProgressUseCase.loadShowsProgress()
                        }
                    }

                    watchlistAsync.await()
                    progressAsync.await()

                    isWatchlistState.update {
                        userWatchlistMinLocalSource.containsShow(show.ids.trakt)
                    }
                    isWatchedState.update {
                        val containsShow = userProgressLocalSource.containsShow(show.ids.trakt)
                        if (containsShow) {
                            val progress = userProgressLocalSource.getShows(setOf(show.ids.trakt)).firstOrNull()
                            return@update progress?.isCompleted(show) == true
                        }
                        return@update false
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

                updateWatchlistUseCase.addToWatchlist(showId = show.ids.trakt)
                userWatchlistLocalSource.addShows(
                    listOf(
                        WatchlistItem.ShowItem(
                            rank = 0,
                            show = show,
                            listedAt = nowUtcInstant(),
                        ),
                    ),
                )
                userWatchlistMinLocalSource.addShows(
                    shows = setOf(show.ids.trakt),
                )

                watchlistUpdates.notifyUpdate(Default)

                analytics.progress.logAddWatchlistMedia(
                    mediaType = "show",
                    source = "show_context",
                )

                loadingWatchlistState.update { Done }
            } catch (error: Exception) {
                error.rethrowCancellation {
                    when (error.getHttpCode()) {
                        HTTP_ERROR_TRAKT_VIP_LIMIT -> {
                            errorsManager.tryEmit(error)
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

                updateWatchlistUseCase.removeFromWatchlist(showId = show.ids.trakt)
                userWatchlistLocalSource.removeShows(
                    ids = setOf(show.ids.trakt),
                )
                userWatchlistMinLocalSource.removeShows(
                    ids = setOf(show.ids.trakt),
                )

                watchlistUpdates.notifyUpdate(Default)

                analytics.progress.logRemoveWatchlistMedia(
                    mediaType = "show",
                    source = "list_show_context",
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

                updateHistoryUseCase.addToWatched(
                    showId = show.ids.trakt,
                    customDate = customDate,
                )
                loadProgressUseCase.loadShowsProgress()
                userWatchlistLocalSource.removeShows(
                    ids = setOf(show.ids.trakt),
                )
                userWatchlistMinLocalSource.removeShows(
                    ids = setOf(show.ids.trakt),
                )

                watchlistUpdates.notifyUpdate(Default)

                analytics.progress.logAddWatchedMedia(
                    mediaType = "show",
                    source = "show_context",
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

                updateHistoryUseCase.removeAllFromHistory(show.ids.trakt)
                userProgressLocalSource.removeShows(ids = setOf(show.ids.trakt))
                progressUpdates.notifyUpdate()

                analytics.progress.logRemoveWatchedMedia(
                    mediaType = "show",
                    source = "show_context",
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

    fun clear() {
        loadingWatchedState.update { Idle }
        loadingWatchlistState.update { Idle }

        errorState.update { null }
    }

    private fun isLoading(): Boolean {
        return loadingWatchedState.value.isLoading || loadingWatchlistState.value.isLoading
    }

    val state: StateFlow<ShowContextState> = combine(
        isWatchlistState,
        isWatchedState,
        loadingWatchedState,
        loadingWatchlistState,
        userState,
        errorState,
    ) { state ->
        ShowContextState(
            isWatchlist = state[0] as Boolean,
            isWatched = state[1] as Boolean,
            loadingWatched = state[2] as LoadingState,
            loadingWatchlist = state[3] as LoadingState,
            user = state[4] as User?,
            error = state[5] as Exception?,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = initialState,
    )
}
