package tv.trakt.trakt.core.lists.features.reorder.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.lazy.LazyListItemInfo
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.zIndex
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch

/**
 * Pixels trimmed off the viewport edges when deciding where auto-scroll starts, so a screen can
 * treat an overlay (the main menu bar) as the effective edge of the list rather than the screen.
 */
internal data class DragEdgeInsets(
    val start: Float = 0f,
    val end: Float = 0f,
)

internal class DragDropState(
    private val state: LazyListState,
    private val scope: CoroutineScope,
    private val draggable: (LazyListItemInfo) -> Boolean,
    private val onMove: (from: Int, to: Int) -> Unit,
) {
    var draggingItemIndex by mutableStateOf<Int?>(null)
        private set

    /** Read from the pointer callback, not from composition, so a plain `var` is enough. */
    internal var edgeInsets: DragEdgeInsets = DragEdgeInsets()

    internal val scrollChannel = Channel<Float>(capacity = Channel.CONFLATED)

    private var draggingItemDraggedDelta by mutableFloatStateOf(0f)
    private var draggingItemInitialOffset by mutableIntStateOf(0)

    /**
     * Cached so the gesture survives the dragged slot leaving [LazyListState.layoutInfo]. The
     * visible window only extends by the content padding, which is much smaller at the top of
     * this screen than at the bottom.
     */
    private var draggingItemSize by mutableIntStateOf(0)

    /** Sign of the most recent drag event; the accumulated delta is not a direction. */
    private var lastDragDirection = 0f

    internal val draggingItemOffset: Float
        get() = draggingItemLayoutInfo?.let { item ->
            draggingItemInitialOffset + draggingItemDraggedDelta - item.offset
        } ?: 0f

    private val draggingItemLayoutInfo: LazyListItemInfo?
        get() = state.layoutInfo.visibleItemsInfo.firstOrNull { it.index == draggingItemIndex }

    internal var previousIndexOfDraggedItem by mutableStateOf<Int?>(null)
        private set
    internal val previousItemOffset = Animatable(0f)

    internal fun onDragStart(index: Int) {
        val item = state.layoutInfo.visibleItemsInfo.firstOrNull { it.index == index } ?: return
        draggingItemIndex = index
        draggingItemInitialOffset = item.offset
        draggingItemSize = item.size
    }

    internal fun onDragInterrupted() {
        val dragging = draggingItemIndex
        if (dragging != null) {
            previousIndexOfDraggedItem = dragging
            val startOffset = draggingItemOffset
            scope.launch {
                previousItemOffset.snapTo(startOffset)
                previousItemOffset.animateTo(
                    targetValue = 0f,
                    animationSpec = spring(
                        stiffness = Spring.StiffnessMediumLow,
                        visibilityThreshold = 1f,
                    ),
                )
                previousIndexOfDraggedItem = null
            }
        }
        draggingItemDraggedDelta = 0f
        draggingItemIndex = null
        draggingItemInitialOffset = 0
        draggingItemSize = 0
        lastDragDirection = 0f
    }

    internal fun onDrag(offsetY: Float) {
        val draggingIndex = draggingItemIndex ?: return

        draggingItemDraggedDelta += offsetY
        if (offsetY != 0f) lastDragDirection = offsetY

        // Refresh the cached size while the slot is still composed.
        draggingItemLayoutInfo?.let { draggingItemSize = it.size }

        // Equivalent to `draggingItem.offset + draggingItemOffset`, without depending on the slot
        // still being reported in visibleItemsInfo: the card sits where the finger left it.
        val startOffset = draggingItemInitialOffset + draggingItemDraggedDelta
        val endOffset = startOffset + draggingItemSize
        val middleOffset = startOffset + draggingItemSize / 2f

        val targetItem = state.layoutInfo.visibleItemsInfo.firstOrNull { item ->
            middleOffset.toInt() in item.offset..(item.offset + item.size) &&
                draggingIndex != item.index &&
                draggable(item)
        }

        if (targetItem != null) {
            // Pin the scroll position by index for the coming remeasure. Otherwise LazyList
            // re-anchors on the first visible item's key, which shoves the dragged slot above the
            // viewport as soon as an upward swap crosses that item.
            state.requestScrollToItem(
                index = state.firstVisibleItemIndex,
                scrollOffset = state.firstVisibleItemScrollOffset,
            )
            onMove(draggingIndex, targetItem.index)
            draggingItemIndex = targetItem.index
            return
        }

        val startEdge = state.layoutInfo.viewportStartOffset + edgeInsets.start
        val endEdge = state.layoutInfo.viewportEndOffset - edgeInsets.end

        val overscroll = when {
            lastDragDirection > 0 -> {
                (endOffset - endEdge).coerceAtLeast(0f)
            }

            lastDragDirection < 0 -> {
                (startOffset - startEdge).coerceAtMost(0f)
            }

            else -> {
                0f
            }
        }
        if (overscroll != 0f) scrollChannel.trySend(overscroll)
    }
}

@Composable
internal fun rememberDragDropState(
    lazyListState: LazyListState,
    scope: CoroutineScope,
    edgeInsets: DragEdgeInsets = DragEdgeInsets(),
    draggable: (LazyListItemInfo) -> Boolean = { true },
    onMove: (from: Int, to: Int) -> Unit,
): DragDropState {
    val dragDropState = remember(lazyListState) {
        DragDropState(
            state = lazyListState,
            scope = scope,
            draggable = draggable,
            onMove = onMove,
        )
    }

    SideEffect {
        dragDropState.edgeInsets = edgeInsets
    }

    return dragDropState
}

@Composable
internal fun LazyItemScope.DraggableItem(
    dragDropState: DragDropState,
    index: Int,
    modifier: Modifier = Modifier,
    content: @Composable (isDragging: Boolean) -> Unit,
) {
    val dragging = index == dragDropState.draggingItemIndex
    val draggingModifier = when {
        dragging -> {
            Modifier
                .zIndex(1f)
                .graphicsLayer { translationY = dragDropState.draggingItemOffset }
        }

        index == dragDropState.previousIndexOfDraggedItem -> {
            Modifier
                .zIndex(1f)
                .graphicsLayer { translationY = dragDropState.previousItemOffset.value }
        }

        else -> {
            Modifier.animateItem(
                fadeInSpec = null,
                fadeOutSpec = null,
            )
        }
    }

    Box(modifier = modifier.then(draggingModifier)) {
        content(dragging)
    }
}

internal fun Modifier.dragHandle(
    state: DragDropState,
    index: State<Int>,
    haptic: HapticFeedback,
): Modifier =
    pointerInput(state) {
        detectDragGestures(
            onDragStart = {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                state.onDragStart(index.value)
            },
            onDrag = { change, dragAmount ->
                change.consume()
                state.onDrag(dragAmount.y)
            },
            onDragEnd = { state.onDragInterrupted() },
            onDragCancel = { state.onDragInterrupted() },
        )
    }
