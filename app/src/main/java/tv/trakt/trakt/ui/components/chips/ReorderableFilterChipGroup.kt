package tv.trakt.trakt.ui.components.chips

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import tv.trakt.trakt.ui.components.reorder.DraggableItem
import tv.trakt.trakt.ui.components.reorder.horizontalDragHandle
import tv.trakt.trakt.ui.components.reorder.rememberDragDropState
import tv.trakt.trakt.ui.theme.TraktTheme

/** Degrees the idle chips rock through while the row is in reorder mode. */
private const val WIGGLE_DEGREES = 2F

private const val WIGGLE_DURATION = 170

/** How much the chip under the finger grows, so it reads as lifted off the row. */
private const val DRAGGING_SCALE = 1.06F

/**
 * A [FilterChipGroup] whose chips can be dragged into a new order.
 *
 * A long press on a chip calls [onEnterReorder] and slides straight into the drag; once
 * [reordering] is on, a plain horizontal swipe is enough. The caller owns [reordering] and the
 * committed [items]; this only tracks the order for the length of a gesture and hands the result
 * back through [onReorder] once the chip is dropped.
 */
@Composable
internal fun <T : Any> ReorderableFilterChipGroup(
    items: ImmutableList<T>,
    key: (T) -> Any,
    reordering: Boolean,
    modifier: Modifier = Modifier,
    paddingVertical: PaddingValues = PaddingValues(top = 13.dp, bottom = 15.dp),
    paddingHorizontal: PaddingValues = PaddingValues(),
    onEnterReorder: () -> Unit = {},
    onReorder: (ImmutableList<T>) -> Unit = {},
    chip: @Composable (item: T, dragging: Boolean, chipModifier: Modifier) -> Unit,
) {
    val scope = rememberCoroutineScope()
    val haptic = LocalHapticFeedback.current
    val listState = rememberLazyListState()
    val reversed = LocalLayoutDirection.current == LayoutDirection.Rtl

    // The order under the finger. Committed orders arrive back through `items`.
    var order by remember { mutableStateOf(items) }
    val currentOrder by rememberUpdatedState(order)
    val currentOnReorder by rememberUpdatedState(onReorder)
    val currentOnEnterReorder by rememberUpdatedState(onEnterReorder)
    val active = rememberUpdatedState(reordering)

    LaunchedEffect(items) {
        order = items
    }

    val dragDropState = rememberDragDropState(
        lazyListState = listState,
        scope = scope,
        onMove = { fromIndex, toIndex ->
            order = order
                .toMutableList()
                .apply { add(toIndex, removeAt(fromIndex)) }
                .toImmutableList()
        },
    )

    LaunchedEffect(dragDropState) {
        while (true) {
            val diff = dragDropState.scrollChannel.receive()
            listState.scrollBy(diff)
        }
    }

    val wiggle by rememberInfiniteTransition(label = "wiggle").animateFloat(
        initialValue = -WIGGLE_DEGREES,
        targetValue = WIGGLE_DEGREES,
        animationSpec = infiniteRepeatable(
            animation = tween(WIGGLE_DURATION, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "wiggle",
    )

    LazyRow(
        state = listState,
        horizontalArrangement = spacedBy(TraktTheme.spacing.filterChipsSpace),
        verticalAlignment = Alignment.CenterVertically,
        contentPadding = paddingHorizontal,
        userScrollEnabled = !reordering,
        overscrollEffect = null,
        modifier = modifier.padding(paddingVertical),
    ) {
        itemsIndexed(
            items = order,
            key = { _, item -> key(item) },
        ) { index, item ->
            DraggableItem(
                dragDropState = dragDropState,
                index = index,
                orientation = Orientation.Horizontal,
                reversed = reversed,
            ) { dragging ->
                val indexState = rememberUpdatedState(index)

                val handleModifier = Modifier.horizontalDragHandle(
                    state = dragDropState,
                    index = indexState,
                    active = active,
                    haptic = haptic,
                    reversed = reversed,
                    onActivate = { currentOnEnterReorder() },
                    onDragStopped = { currentOnReorder(currentOrder) },
                )

                chip(
                    item,
                    dragging,
                    handleModifier.graphicsLayer {
                        // Neighbours rock in opposite phase, so the row reads as restless rather
                        // than as one block sliding.
                        rotationZ = when {
                            !reordering || dragging -> 0F
                            index % 2 == 0 -> wiggle
                            else -> -wiggle
                        }
                        val scale = if (dragging) DRAGGING_SCALE else 1F
                        scaleX = scale
                        scaleY = scale
                    },
                )
            }
        }
    }
}
