package tv.trakt.trakt.core.today.stories.watched

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
import tv.trakt.trakt.common.firebase.analytics.Analytics
import tv.trakt.trakt.common.helpers.LoadingState
import tv.trakt.trakt.common.helpers.LoadingState.Done
import tv.trakt.trakt.common.helpers.LoadingState.Loading
import tv.trakt.trakt.common.helpers.extensions.recordError
import tv.trakt.trakt.common.helpers.extensions.rethrowCancellation
import tv.trakt.trakt.core.today.model.TodayForYouItem
import tv.trakt.trakt.core.today.usecases.UpdateTodayHistoryUseCase

private const val ANALYTICS_SOURCE = "today_stories"

/** For You items are unwatched by definition, so the button starts unwatched. */
@Suppress("UNCHECKED_CAST")
internal class TodayWatchedViewModel(
    private val item: TodayForYouItem,
    private val updateHistoryUseCase: UpdateTodayHistoryUseCase,
    private val analytics: Analytics,
) : ViewModel() {
    private val initialState = TodayWatchedState()

    private val isWatchedState = MutableStateFlow(initialState.isWatched)
    private val loadingState = MutableStateFlow(initialState.loading)

    private val eventsFlow = MutableSharedFlow<TodayWatchedEvent>(replay = 0)
    val events = eventsFlow.asSharedFlow()

    private val mediaType: String
        get() = when (item) {
            is TodayForYouItem.UpNext -> "episode"
            is TodayForYouItem.StartWatching -> item.media.type.value
        }

    fun addToWatched() {
        if (loadingState.value.isLoading) return

        viewModelScope.launch {
            loadingState.update { Loading }
            isWatchedState.update { true }

            try {
                updateHistoryUseCase.addToWatched(item)
                eventsFlow.emit(TodayWatchedEvent.Added)
                analytics.progress.logAddWatchedMedia(
                    mediaType = mediaType,
                    source = ANALYTICS_SOURCE,
                    date = null,
                )
            } catch (error: Exception) {
                error.rethrowCancellation {
                    isWatchedState.update { false }
                    Timber.recordError(error)
                }
            } finally {
                loadingState.update { Done }
            }
        }
    }

    fun removeFromWatched() {
        if (loadingState.value.isLoading) return

        viewModelScope.launch {
            loadingState.update { Loading }
            isWatchedState.update { false }

            try {
                updateHistoryUseCase.removeFromWatched(item)
                eventsFlow.emit(TodayWatchedEvent.Removed)
                analytics.progress.logRemoveWatchedMedia(
                    mediaType = mediaType,
                    source = ANALYTICS_SOURCE,
                )
            } catch (error: Exception) {
                error.rethrowCancellation {
                    isWatchedState.update { true }
                    Timber.recordError(error)
                }
            } finally {
                loadingState.update { Done }
            }
        }
    }

    val state = combine<Any?, TodayWatchedState>(
        isWatchedState,
        loadingState,
    ) { state ->
        TodayWatchedState(
            isWatched = state[0] as Boolean,
            loading = state[1] as LoadingState,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = initialState,
    )
}
