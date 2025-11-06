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
import tv.trakt.trakt.analytics.Analytics
import tv.trakt.trakt.common.auth.session.SessionManager
import tv.trakt.trakt.common.helpers.LoadingState
import tv.trakt.trakt.common.helpers.LoadingState.DONE
import tv.trakt.trakt.common.helpers.LoadingState.IDLE
import tv.trakt.trakt.common.helpers.LoadingState.LOADING
import tv.trakt.trakt.common.helpers.extensions.nowUtcInstant
import tv.trakt.trakt.common.helpers.extensions.rethrowCancellation
import tv.trakt.trakt.common.model.CustomList
import tv.trakt.trakt.common.model.Show
import tv.trakt.trakt.core.lists.sections.personal.usecases.RemovePersonalListItemUseCase
import tv.trakt.trakt.core.lists.sections.watchlist.model.WatchlistItem
import tv.trakt.trakt.core.sync.usecases.UpdateShowHistoryUseCase
import tv.trakt.trakt.core.sync.usecases.UpdateShowWatchlistUseCase
import tv.trakt.trakt.core.user.data.local.UserProgressLocalDataSource
import tv.trakt.trakt.core.user.data.local.UserWatchlistLocalDataSource
import tv.trakt.trakt.core.user.usecases.lists.LoadUserWatchlistUseCase
import tv.trakt.trakt.core.user.usecases.progress.LoadUserProgressUseCase

internal class ListShowContextViewModel(
    private val show: Show,
    private val list: CustomList,
    private val updateShowWatchlistUseCase: UpdateShowWatchlistUseCase,
    private val updateShowHistoryUseCase: UpdateShowHistoryUseCase,
    private val removeListItemUseCase: RemovePersonalListItemUseCase,
    private val userProgressLocalSource: UserProgressLocalDataSource,
    private val userWatchlistLocalSource: UserWatchlistLocalDataSource,
    private val loadProgressUseCase: LoadUserProgressUseCase,
    private val loadWatchlistUseCase: LoadUserWatchlistUseCase,
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
                loadingWatchedState.update { LOADING }
                loadingWatchlistState.update { LOADING }
                loadingListState.update { LOADING }

                coroutineScope {
                    val watchlistAsync = async {
                        if (!userWatchlistLocalSource.isShowsLoaded()) {
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
                        userWatchlistLocalSource.containsShow(show.ids.trakt)
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
                    Timber.e(error)
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
                    notify = true,
                )

                analytics.progress.logAddWatchlistMedia(
                    mediaType = "show",
                    source = "list_show_context",
                )
            } catch (error: Exception) {
                error.rethrowCancellation {
                    errorState.update { error }
                    Timber.e(error)
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

                updateShowWatchlistUseCase.removeFromWatchlist(
                    showId = show.ids.trakt,
                )
                userWatchlistLocalSource.removeShows(
                    ids = setOf(show.ids.trakt),
                    notify = true,
                )

                analytics.progress.logRemoveWatchlistMedia(
                    mediaType = "show",
                    source = "list_show_context",
                )
            } catch (error: Exception) {
                error.rethrowCancellation {
                    errorState.update { error }
                    Timber.e(error)
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

                updateShowHistoryUseCase.addToWatched(show.ids.trakt)
                loadProgressUseCase.loadShowsProgress()
                userWatchlistLocalSource.removeShows(
                    ids = setOf(show.ids.trakt),
                    notify = true,
                )

                analytics.progress.logAddWatchedMedia(
                    mediaType = "show",
                    source = "list_show_context",
                )
            } catch (error: Exception) {
                error.rethrowCancellation {
                    errorState.update { error }
                    Timber.e(error)
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

                updateShowHistoryUseCase.removeAllFromHistory(show.ids.trakt)
                userProgressLocalSource.removeShows(setOf(show.ids.trakt))

                analytics.progress.logRemoveWatchedMedia(
                    mediaType = "show",
                    source = "list_show_context",
                )
            } catch (error: Exception) {
                error.rethrowCancellation {
                    errorState.update { error }
                    Timber.e(error)
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

                removeListItemUseCase.removeShow(
                    listId = list.ids.trakt,
                    showId = show.ids.trakt,
                )
            } catch (error: Exception) {
                error.rethrowCancellation {
                    errorState.update { error }
                    Timber.e(error)
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
