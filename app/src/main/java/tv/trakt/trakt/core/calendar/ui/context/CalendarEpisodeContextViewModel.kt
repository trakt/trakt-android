package tv.trakt.trakt.core.calendar.ui.context

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import tv.trakt.trakt.common.auth.session.SessionManager
import tv.trakt.trakt.common.core.lists.model.WatchlistItem
import tv.trakt.trakt.common.core.sync.changes.DataChange.Kind
import tv.trakt.trakt.common.core.sync.changes.DataChanges
import tv.trakt.trakt.common.core.user.usecases.lists.LoadUserWatchlistUseCase
import tv.trakt.trakt.common.core.user.usecases.progress.LoadUserProgressUseCase
import tv.trakt.trakt.common.data.local.HomeUpNextLocalSource
import tv.trakt.trakt.common.data.local.HomeUpcomingLocalSource
import tv.trakt.trakt.common.data.local.UserProgressLocalSource
import tv.trakt.trakt.common.data.local.UserWatchlistLocalSource
import tv.trakt.trakt.common.data.local.UserWatchlistMinimalLocalSource
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
import tv.trakt.trakt.common.model.User
import tv.trakt.trakt.common.model.calendar.CalendarItem
import tv.trakt.trakt.core.sync.usecases.UpdateEpisodeHistoryUseCase
import tv.trakt.trakt.core.sync.usecases.UpdateShowHistoryUseCase
import tv.trakt.trakt.core.sync.usecases.UpdateShowWatchlistUseCase

@Suppress("UNCHECKED_CAST")
internal class CalendarEpisodeContextViewModel(
    private val item: CalendarItem.EpisodeItem,
    private val updateHistoryUseCase: UpdateEpisodeHistoryUseCase,
    private val updateWatchlistUseCase: UpdateShowWatchlistUseCase,
    private val updateShowHistoryUseCase: UpdateShowHistoryUseCase,
    private val dataChanges: DataChanges,
    private val userProgressLocalSource: UserProgressLocalSource,
    private val userWatchlistMinLocalSource: UserWatchlistMinimalLocalSource,
    private val loadProgressUseCase: LoadUserProgressUseCase,
    private val loadWatchlistUseCase: LoadUserWatchlistUseCase,
    private val sessionManager: SessionManager,
    private val errorsManager: GlobalErrorsManager,
    private val analytics: Analytics,
) : ViewModel() {
    private val initialState = CalendarEpisodeContextState()

    private val isWatchedState = MutableStateFlow(item.watched)
    private val isWatchlistState = MutableStateFlow(initialState.isWatchlist)
    private val loadingWatchedState = MutableStateFlow(initialState.loadingWatched)
    private val loadingWatchlistState = MutableStateFlow(initialState.loadingWatchlist)
    private val loadingDropState = MutableStateFlow(initialState.loadingDrop)
    private val errorState = MutableStateFlow(initialState.error)

    private val showId = item.show.ids.trakt

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
                }

                isWatchlistState.update {
                    userWatchlistMinLocalSource.containsShow(showId)
                }
                isWatchedState.update {
                    val progress = userProgressLocalSource.getShows(setOf(showId)).firstOrNull()
                        ?: return@update false

                    item.episodes.all { episode ->
                        progress.isEpisodeWatched(
                            seasonNumber = episode.season,
                            episodeId = episode.ids.trakt,
                        )
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

    fun addToWatched(customDate: DateSelectionResult? = null) {
        if (isLoading()) {
            return
        }

        viewModelScope.launch {
            clear()
            try {
                loadingWatchedState.update { Loading }

                updateHistoryUseCase.addToHistory(
                    episodeIds = item.episodes.map { it.ids.trakt },
                    customDate = customDate,
                )
                loadProgressUseCase.loadShowsProgress()
                dataChanges.notify(Kind.History)

                analytics.progress.logAddWatchedMedia(
                    mediaType = "episode",
                    source = "calendar_episode_context",
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

                item.episodes.forEach { episode ->
                    updateHistoryUseCase.removeEpisodeFromHistory(episode.ids.trakt.value)
                }
                loadProgressUseCase.loadShowsProgress()
                dataChanges.notify(Kind.History)

                analytics.progress.logRemoveWatchedMedia(
                    mediaType = "episode",
                    source = "calendar_episode_context",
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

    fun addToWatchlist() {
        if (isLoading()) {
            return
        }

        viewModelScope.launch {
            clear()
            try {
                loadingWatchlistState.update { Loading }

                updateWatchlistUseCase.addToWatchlist(item.show)

                analytics.progress.logAddWatchlistMedia(
                    mediaType = "show",
                    source = "calendar_episode_context",
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

                updateWatchlistUseCase.removeFromWatchlist(showId = showId)

                analytics.progress.logRemoveWatchlistMedia(
                    mediaType = "show",
                    source = "calendar_episode_context",
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

    fun dropShow() {
        if (isLoading()) {
            return
        }

        viewModelScope.launch {
            clear()
            try {
                loadingDropState.update { Loading }

                updateShowHistoryUseCase.dropShow(showId)
            } catch (error: Exception) {
                error.rethrowCancellation {
                    errorState.update { error }
                    Timber.recordError(error)
                }
            } finally {
                loadingDropState.update { Done }
            }
        }
    }

    private fun clear() {
        loadingWatchedState.update { Idle }
        loadingWatchlistState.update { Idle }
        loadingDropState.update { Idle }
        errorState.update { null }
    }

    private fun isLoading(): Boolean {
        return loadingWatchedState.value.isLoading ||
            loadingWatchlistState.value.isLoading ||
            loadingDropState.value.isLoading
    }

    val state = combine(
        isWatchedState,
        isWatchlistState,
        loadingWatchedState,
        loadingWatchlistState,
        loadingDropState,
        sessionManager.observeProfile(),
        errorState,
    ) { state ->
        CalendarEpisodeContextState(
            isWatched = state[0] as Boolean,
            isWatchlist = state[1] as Boolean,
            loadingWatched = state[2] as LoadingState,
            loadingWatchlist = state[3] as LoadingState,
            loadingDrop = state[4] as LoadingState,
            user = state[5] as User?,
            error = state[6] as Exception?,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = initialState,
    )
}
