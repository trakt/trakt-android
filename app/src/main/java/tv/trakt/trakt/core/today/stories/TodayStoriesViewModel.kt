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
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import tv.trakt.trakt.common.firebase.analytics.Analytics
import tv.trakt.trakt.common.helpers.LoadingState
import tv.trakt.trakt.common.helpers.LoadingState.Done
import tv.trakt.trakt.common.helpers.LoadingState.Loading
import tv.trakt.trakt.common.helpers.extensions.nowUtcInstant
import tv.trakt.trakt.common.helpers.extensions.recordError
import tv.trakt.trakt.common.helpers.extensions.rethrowCancellation
import tv.trakt.trakt.common.model.MediaMode
import tv.trakt.trakt.core.today.data.local.TodaySeenLocalDataSource
import tv.trakt.trakt.core.today.model.TodayStoryGroup
import tv.trakt.trakt.core.today.model.analyticsType
import tv.trakt.trakt.core.today.stories.navigation.TodayStoriesDestination
import tv.trakt.trakt.core.today.usecases.GetTodayStoriesUseCase
import java.time.Duration
import java.time.Instant

private const val OPEN_SOURCE = "navbar"

private const val CLOSE_REASON_CLOSE = "close"
private const val CLOSE_REASON_END = "end"
private const val CLOSE_REASON_LEAVE = "leave"

internal sealed interface TodayStoriesEvent {
    data object Close : TodayStoriesEvent
}

@Suppress("UNCHECKED_CAST")
internal class TodayStoriesViewModel(
    savedStateHandle: SavedStateHandle,
    private val getTodayStoriesUseCase: GetTodayStoriesUseCase,
    private val seenSource: TodaySeenLocalDataSource,
    private val analytics: Analytics,
) : ViewModel() {
    private val destination = savedStateHandle.toRoute<TodayStoriesDestination>()
    private val initialState = TodayStoriesState(
        position = TodayStoryPosition(groupKey = destination.startKey),
    )

    private val groupsState = MutableStateFlow(initialState.groups)
    private val positionState = MutableStateFlow(initialState.position)
    private val loadingState = MutableStateFlow(initialState.loading)

    private var openedAt: Instant? = null
    private var shownFrames = emptySet<String>()
    private var shownGroups = emptySet<String>()

    val events: Flow<TodayStoriesEvent>
        field = MutableSharedFlow<TodayStoriesEvent>(replay = 0)

    init {
        loadData()

        analytics.logScreenView(
            screenName = "today_stories",
        )
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

                val seenAt = seenSource.observeSeen().first()
                startTracking(hasUnseen = groups.any { !it.isSeen(seenAt) })
                enterGroup(group)
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
            else -> close(CLOSE_REASON_END)
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
            close(CLOSE_REASON_END)
            return
        }
        showGroup(current.groupIndex + 1, 0)
    }

    fun previousGroup() {
        val current = current()
        if (current.groupIndex <= 0) {
            close(CLOSE_REASON_CLOSE)
            return
        }
        showGroup(current.groupIndex - 1, 0)
    }

    /** Called before navigating out of the viewer, e.g. to a user profile. */
    fun leave() {
        finishTracking(CLOSE_REASON_LEAVE)
    }

    private fun close(reason: String) {
        finishTracking(reason)
        viewModelScope.launch { events.emit(TodayStoriesEvent.Close) }
    }

    private fun showFrame(index: Int) {
        positionState.update { it.copy(frameIndex = index) }
        noteShown()
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
        enterGroup(group)
        markSeen(group.key)
    }

    private fun startTracking(hasUnseen: Boolean) {
        openedAt = nowUtcInstant()
        analytics.today.logOpen(
            source = OPEN_SOURCE,
            hasUnseen = hasUnseen,
        )
    }

    private fun enterGroup(group: TodayStoryGroup) {
        if (openedAt == null) return

        analytics.today.logStory(type = group.analyticsType)
        noteShown()
    }

    private fun noteShown() {
        val current = current()
        val group = current.group ?: return
        val frame = current.frame ?: return

        shownGroups = shownGroups + group.key
        shownFrames = shownFrames + frame.key
    }

    private fun finishTracking(reason: String) {
        val opened = openedAt ?: return
        openedAt = null

        analytics.today.logClose(
            reason = reason,
            durationMs = Duration.between(opened, nowUtcInstant()).toMillis(),
            frames = shownFrames.size,
            groups = shownGroups.size,
        )
    }

    override fun onCleared() {
        finishTracking(CLOSE_REASON_CLOSE)
        super.onCleared()
    }

    private fun markSeen(key: String) {
        viewModelScope.launch {
            try {
                seenSource.markSeen(keys = listOf(key), seenAt = nowUtcInstant())
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
