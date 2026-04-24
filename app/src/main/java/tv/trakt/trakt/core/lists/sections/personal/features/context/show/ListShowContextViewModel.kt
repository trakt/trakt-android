package tv.trakt.trakt.core.lists.sections.personal.features.context.show

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
import tv.trakt.trakt.common.helpers.extensions.nowUtcInstant
import tv.trakt.trakt.common.helpers.extensions.rethrowCancellation
import tv.trakt.trakt.common.model.CustomList
import tv.trakt.trakt.common.model.Show
import tv.trakt.trakt.core.lists.sections.personal.usecases.manage.RemovePersonalListItemUseCase
import tv.trakt.trakt.core.lists.sections.watchlist.model.WatchlistItem
import tv.trakt.trakt.core.sync.usecases.UpdateShowHistoryUseCase
import tv.trakt.trakt.core.sync.usecases.UpdateShowWatchlistUseCase
import tv.trakt.trakt.core.user.data.local.UserProgressLocalDataSource
import tv.trakt.trakt.core.user.data.local.watchlist.UserWatchlistLocalDataSource
import tv.trakt.trakt.core.user.data.local.watchlist.WatchlistUpdates
import tv.trakt.trakt.core.user.data.local.watchlist.WatchlistUpdates.Source
import tv.trakt.trakt.core.user.data.local.watchlist.minimal.UserWatchlistMinimalLocalDataSource
import tv.trakt.trakt.core.user.usecases.lists.LoadUserWatchlistUseCase
import tv.trakt.trakt.core.user.usecases.progress.LoadUserProgressUseCase
import tv.trakt.trakt.ui.components.dateselection.DateSelectionResult

internal class ListShowContextViewModel(
    private val show: Show,
    private val list: CustomList,
    private val updateShowWatchlistUseCase: UpdateShowWatchlistUseCase,
    private val updateShowHistoryUseCase: UpdateShowHistoryUseCase,
    private val removeListItemUseCase: RemovePersonalListItemUseCase,
    private val userProgressLocalSource: UserProgressLocalDataSource,
    private val userWatchlistLocalSource: UserWatchlistLocalDataSource,
    private val userWatchlistMinLocalSource: UserWatchlistMinimalLocalDataSource,
    private val loadProgressUseCase: LoadUserProgressUseCase,
    private val loadWatchlistMinUseCase: LoadUserWatchlistUseCase,
    private val watchlistUpdates: WatchlistUpdates,
    private val sessionManager: SessionManager,
    private val analytics: Analytics,
) : ViewModel() {
    private val initialState = ListShowContextState()

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
                loadingWatchedState.update { Loading }
                loadingWatchlistState.update { Loading }
                loadingListState.update { Loading }

                coroutineScope {
                    val watchlistMinAsync = async {
                        if (!loadWatchlistMinUseCase.isShowsLoaded()) {
                            loadWatchlistMinUseCase.loadWatchlist()
                        }
                    }

                    val progressAsync = async {
                        if (!userProgressLocalSource.isShowsLoaded()) {
                            loadProgressUseCase.loadShowsProgress()
                        }
                    }

                    watchlistMinAsync.await()
                    progressAsync.await()

                    isWatchlistState.update {
                        userWatchlistMinLocalSource.containsShow(show.ids.trakt)
                    }
                    isWatchedState.update {
                        val containsShow = userProgressLocalSource.containsShow(show.ids.trakt)
                        if (containsShow) {
                            val show = userProgressLocalSource.getShows(setOf(show.ids.trakt)).firstOrNull()
                            return@update show?.isCompleted == true
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

                updateShowWatchlistUseCase.addToWatchlist(
                    showId = show.ids.trakt,
                )

                userWatchlistLocalSource.addShows(
                    shows = listOf(
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

                watchlistUpdates.notifyUpdate(Source.Default)

                analytics.progress.logAddWatchlistMedia(
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

    fun removeFromWatchlist() {
        if (isLoading()) {
            return
        }

        viewModelScope.launch {
            clear()
            try {
                loadingWatchlistState.update { Loading }

                updateShowWatchlistUseCase.removeFromWatchlist(
                    showId = show.ids.trakt,
                )

                userWatchlistLocalSource.removeShows(ids = setOf(show.ids.trakt))
                userWatchlistMinLocalSource.removeShows(ids = setOf(show.ids.trakt))

                watchlistUpdates.notifyUpdate(Source.Default)

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

                updateShowHistoryUseCase.addToWatched(
                    showId = show.ids.trakt,
                    customDate = customDate,
                )
                loadProgressUseCase.loadShowsProgress()
                userWatchlistLocalSource.removeShows(ids = setOf(show.ids.trakt))
                userWatchlistMinLocalSource.removeShows(ids = setOf(show.ids.trakt))

                watchlistUpdates.notifyUpdate(Source.Default)

                analytics.progress.logAddWatchedMedia(
                    mediaType = "show",
                    source = "list_show_context",
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

                updateShowHistoryUseCase.removeAllFromHistory(show.ids.trakt)
                userProgressLocalSource.removeShows(setOf(show.ids.trakt))

                analytics.progress.logRemoveWatchedMedia(
                    mediaType = "show",
                    source = "list_show_context",
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
        if (isLoading()) {
            return
        }

        viewModelScope.launch {
            clear()
            try {
                loadingListState.update { Loading }

                removeListItemUseCase.removeShow(
                    listId = list.ids.trakt,
                    ownerId = list.user.ids.trakt,
                    showId = show.ids.trakt,
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
        loadingListState.update { Idle }

        errorState.update { null }
    }

    private fun isLoading(): Boolean {
        return loadingWatchedState.value.isLoading ||
            loadingWatchlistState.value.isLoading ||
            loadingListState.value.isLoading
    }

    val state: StateFlow<ListShowContextState> = combine(
        isWatchlistState,
        isWatchedState,
        loadingWatchedState,
        loadingWatchlistState,
        loadingListState,
        errorState,
    ) { state ->
        ListShowContextState(
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
