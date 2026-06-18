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

internal class DragDropState(
    private val state: LazyListState,
    private val scope: CoroutineScope,
    private val draggable: (LazyListItemInfo) -> Boolean,
    private val onMove: (from: Int, to: Int) -> Unit,
) {
    var draggingItemIndex by mutableStateOf<Int?>(null)
        private set

    internal val scrollChannel = Channel<Float>()

    private var draggingItemDraggedDelta by mutableFloatStateOf(0f)
    private var draggingItemInitialOffset by mutableIntStateOf(0)

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
    }

    internal fun onDrag(offsetY: Float) {
        draggingItemDraggedDelta += offsetY

        val draggingItem = draggingItemLayoutInfo ?: return
        val startOffset = draggingItem.offset + draggingItemOffset
        val endOffset = startOffset + draggingItem.size
        val middleOffset = startOffset + (endOffset - startOffset) / 2f

        val targetItem = state.layoutInfo.visibleItemsInfo.firstOrNull { item ->
            middleOffset.toInt() in item.offset..(item.offset + item.size) &&
                draggingItem.index != item.index &&
                draggable(item)
        }

        if (targetItem != null) {
            onMove(draggingItem.index, targetItem.index)
            draggingItemIndex = targetItem.index
            return
        }

        val overscroll = when {
            draggingItemDraggedDelta > 0 -> {
                (endOffset - state.layoutInfo.viewportEndOffset).coerceAtLeast(0f)
            }

            draggingItemDraggedDelta < 0 -> {
                (startOffset - state.layoutInfo.viewportStartOffset).coerceAtMost(0f)
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
    draggable: (LazyListItemInfo) -> Boolean = { true },
    onMove: (from: Int, to: Int) -> Unit,
): DragDropState =
    remember(lazyListState) {
        DragDropState(
            state = lazyListState,
            scope = scope,
            draggable = draggable,
            onMove = onMove,
        )
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
