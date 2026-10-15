package tv.trakt.trakt.core.today.stories

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import kotlinx.collections.immutable.ImmutableList
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import tv.trakt.trakt.common.helpers.LoadingState
import tv.trakt.trakt.common.helpers.LoadingState.Done
import tv.trakt.trakt.common.helpers.LoadingState.Loading
import tv.trakt.trakt.common.helpers.extensions.nowUtcInstant
import tv.trakt.trakt.common.helpers.extensions.recordError
import tv.trakt.trakt.common.helpers.extensions.rethrowCancellation
import tv.trakt.trakt.common.model.MediaMode
import tv.trakt.trakt.core.today.data.local.TodaySeenLocalDataSource
import tv.trakt.trakt.core.today.model.TodayStoryGroup
import tv.trakt.trakt.core.today.stories.navigation.TodayStoriesDestination
import tv.trakt.trakt.core.today.usecases.GetTodayStoriesUseCase

internal sealed interface TodayStoriesEvent {
    data object Close : TodayStoriesEvent
}

@Suppress("UNCHECKED_CAST")
internal class TodayStoriesViewModel(
    savedStateHandle: SavedStateHandle,
    private val getTodayStoriesUseCase: GetTodayStoriesUseCase,
    private val seenSource: TodaySeenLocalDataSource,
) : ViewModel() {
    private val destination = savedStateHandle.toRoute<TodayStoriesDestination>()
    private val initialState = TodayStoriesState(
        position = TodayStoryPosition(groupKey = destination.startKey),
    )

    private val groupsState = MutableStateFlow(initialState.groups)
    private val positionState = MutableStateFlow(initialState.position)
    private val loadingState = MutableStateFlow(initialState.loading)

    val events: Flow<TodayStoriesEvent>
        field = MutableSharedFlow<TodayStoriesEvent>(replay = 0)

    init {
        loadData()
    }

    // Groups are a snapshot taken on open, so stories do not reshuffle while being watched.
    private fun loadData() {
        viewModelScope.launch {
            try {
                val groups = getTodayStoriesUseCase
                    .getLocalStories(MediaMode.Media)
                    .ifEmpty {
                        loadingState.update { Loading }
                        getTodayStoriesUseCase.getStories(MediaMode.Media)
                    }

                groupsState.update { groups }

                val group = current().group
                if (group == null) {
                    events.emit(TodayStoriesEvent.Close)
                    return@launch
                }
                markSeen(group.key)
            } catch (error: Exception) {
                error.rethrowCancellation {
                    Timber.recordError(error)
                }
                events.emit(TodayStoriesEvent.Close)
            } finally {
                loadingState.update { Done }
            }
        }
    }

    fun next() {
        val current = current()
        val group = current.group ?: return

        when {
            current.frameIndex < group.frames.lastIndex -> showFrame(current.frameIndex + 1)
            current.groupIndex < current.groups.lastIndex -> showGroup(current.groupIndex + 1, 0)
            else -> viewModelScope.launch { events.emit(TodayStoriesEvent.Close) }
        }
    }

    fun previous() {
        val current = current()

        if (current.frameIndex > 0) {
            showFrame(current.frameIndex - 1)
            return
        }

        val previous = current.groups.getOrNull(current.groupIndex - 1) ?: return
        showGroup(current.groupIndex - 1, previous.frames.lastIndex)
    }

    fun nextGroup() {
        val current = current()
        if (current.groupIndex >= current.groups.lastIndex) {
            viewModelScope.launch { events.emit(TodayStoriesEvent.Close) }
            return
        }
        showGroup(current.groupIndex + 1, 0)
    }

    fun previousGroup() {
        val current = current()
        if (current.groupIndex <= 0) {
            viewModelScope.launch { events.emit(TodayStoriesEvent.Close) }
            return
        }
        showGroup(current.groupIndex - 1, 0)
    }

    private fun showFrame(index: Int) {
        positionState.update { it.copy(frameIndex = index) }
    }

    private fun showGroup(
        index: Int,
        frameIndex: Int,
    ) {
        val group = groupsState.value.getOrNull(index) ?: return

        positionState.update {
            TodayStoryPosition(
                groupKey = group.key,
                frameIndex = frameIndex,
            )
        }
        markSeen(group.key)
    }

    private fun markSeen(key: String) {
        viewModelScope.launch {
            try {
                seenSource.markSeen(key = key, seenAt = nowUtcInstant())
            } catch (error: Exception) {
                error.rethrowCancellation {
                    Timber.recordError(error)
                }
            }
        }
    }

    private fun current(): TodayStoriesState {
        return TodayStoriesState(
            groups = groupsState.value,
            position = positionState.value,
        )
    }

    val state = combine(
        groupsState,
        positionState,
        loadingState,
    ) { state ->
        TodayStoriesState(
            groups = state[0] as ImmutableList<TodayStoryGroup>,
            position = state[1] as TodayStoryPosition,
            loading = state[2] as LoadingState,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = initialState,
    )
}
