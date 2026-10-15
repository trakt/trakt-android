package tv.trakt.trakt.core.today.stories.watchlist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import tv.trakt.trakt.common.auth.session.SessionManager
import tv.trakt.trakt.common.core.user.data.local.watchlist.WatchlistUpdates
import tv.trakt.trakt.common.core.user.data.local.watchlist.WatchlistUpdates.Source.Default
import tv.trakt.trakt.common.firebase.analytics.Analytics
import tv.trakt.trakt.common.helpers.LoadingState
import tv.trakt.trakt.common.helpers.LoadingState.Done
import tv.trakt.trakt.common.helpers.LoadingState.Loading
import tv.trakt.trakt.common.helpers.errors.GlobalErrorsManager
import tv.trakt.trakt.common.helpers.extensions.HTTP_ERROR_TRAKT_VIP_LIMIT
import tv.trakt.trakt.common.helpers.extensions.getHttpCode
import tv.trakt.trakt.common.helpers.extensions.recordError
import tv.trakt.trakt.common.helpers.extensions.rethrowCancellation
import tv.trakt.trakt.core.today.model.TodayMedia
import tv.trakt.trakt.core.today.usecases.UpdateTodayWatchlistUseCase

private const val ANALYTICS_SOURCE = "today_stories"

@Suppress("UNCHECKED_CAST")
internal class TodayWatchlistViewModel(
    private val media: TodayMedia,
    private val updateWatchlistUseCase: UpdateTodayWatchlistUseCase,
    private val watchlistUpdates: WatchlistUpdates,
    private val sessionManager: SessionManager,
    private val errorsManager: GlobalErrorsManager,
    private val analytics: Analytics,
) : ViewModel() {
    private val initialState = TodayWatchlistState()

    private val isWatchlistedState = MutableStateFlow(initialState.isWatchlisted)
    private val loadingState = MutableStateFlow(initialState.loading)

    private val eventsFlow = MutableSharedFlow<TodayWatchlistEvent>(replay = 0)
    val events = eventsFlow.asSharedFlow()

    init {
        loadData()
        observeUpdates()
    }

    private fun loadData() {
        viewModelScope.launch {
            if (!sessionManager.isAuthenticated()) {
                return@launch
            }

            try {
                isWatchlistedState.update {
                    updateWatchlistUseCase.isWatchlisted(media)
                }
            } catch (error: Exception) {
                error.rethrowCancellation {
                    Timber.recordError(error)
                }
            }
        }
    }

    private fun observeUpdates() {
        viewModelScope.launch {
            watchlistUpdates.observeUpdates(Default).collect {
                if (loadingState.value.isLoading) return@collect
                loadData()
            }
        }
    }

    fun addToWatchlist() {
        if (loadingState.value.isLoading) return

        viewModelScope.launch {
            loadingState.update { Loading }
            isWatchlistedState.update { true }

            try {
                updateWatchlistUseCase.addToWatchlist(media)
                eventsFlow.emit(TodayWatchlistEvent.Added)
                analytics.progress.logAddWatchlistMedia(
                    mediaType = media.type.value,
                    source = ANALYTICS_SOURCE,
                )
            } catch (error: Exception) {
                error.rethrowCancellation {
                    isWatchlistedState.update { false }
                    when (error.getHttpCode()) {
                        HTTP_ERROR_TRAKT_VIP_LIMIT -> errorsManager.tryEmit(error)
                        else -> Timber.recordError(error)
                    }
                }
            } finally {
                loadingState.update { Done }
            }
        }
    }

    fun removeFromWatchlist() {
        if (loadingState.value.isLoading) return

        viewModelScope.launch {
            loadingState.update { Loading }
            isWatchlistedState.update { false }

            try {
                updateWatchlistUseCase.removeFromWatchlist(media)
                eventsFlow.emit(TodayWatchlistEvent.Removed)
                analytics.progress.logRemoveWatchlistMedia(
                    mediaType = media.type.value,
                    source = ANALYTICS_SOURCE,
                )
            } catch (error: Exception) {
                error.rethrowCancellation {
                    isWatchlistedState.update { true }
                    Timber.recordError(error)
                }
            } finally {
                loadingState.update { Done }
            }
        }
    }

    val state = combine<Any?, TodayWatchlistState>(
        isWatchlistedState,
        loadingState,
    ) { state ->
        TodayWatchlistState(
            isWatchlisted = state[0] as Boolean?,
            loading = state[1] as LoadingState,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = initialState,
    )
}
