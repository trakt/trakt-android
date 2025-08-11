package tv.trakt.trakt.app.common.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.gestures.BringIntoViewSpec
import androidx.compose.foundation.gestures.LocalBringIntoViewSpec
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onPlaced
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import tv.trakt.trakt.app.ui.theme.TraktTheme

@Composable
internal fun PositionFocusLazyRow(
    modifier: Modifier = Modifier,
    state: LazyListState = rememberLazyListState(),
    contentPadding: PaddingValues = PaddingValues(),
    horizontalArrangement: Arrangement.Horizontal = spacedBy(TraktTheme.spacing.mainRowSpace),
    mainContentStart: Dp = TraktTheme.spacing.mainContentStartSpace,
    content: LazyListScope.() -> Unit,
) {
    PositionFocusList(
        mainContentStart = mainContentStart,
        modifier = modifier,
    ) { modifier ->
        LazyRow(
            state = state,
            modifier = modifier.fillMaxWidth(),
            horizontalArrangement = horizontalArrangement,
            contentPadding = contentPadding,
        ) {
            content()
        }
    }
}

@Composable
internal fun PositionFocusList(
    modifier: Modifier = Modifier,
    mainContentStart: Dp,
    content: @Composable (modifier: Modifier) -> Unit,
) {
    val density = LocalDensity.current
    var rowWidth by remember { mutableFloatStateOf(1f) }
    val fraction = mainContentStart.value / rowWidth

    PositionFocusLayoutItem(parentFraction = fraction) {
        content(
            modifier.onPlaced {
                with(density) {
                    rowWidth = it.size.width.toDp().value
                }
            },
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun PositionFocusLayoutItem(
    parentFraction: Float = 0.0f,
    childFraction: Float = 0f,
    content: @Composable () -> Unit,
) {
    // This bring-into-view spec pivots around the center of the scrollable container
    val bringIntoViewSpec = remember(parentFraction, childFraction) {
        object : BringIntoViewSpec {
            override fun calculateScrollDistance(
                // initial position of item requesting focus
                offset: Float,
                // size of item requesting focus
                size: Float,
                // size of the lazy container
                containerSize: Float,
            ): Float {
                val childSmallerThanParent = size <= containerSize
                val initialTargetForLeadingEdge =
                    parentFraction * containerSize - (childFraction * size)
                val spaceAvailableToShowItem = containerSize - initialTargetForLeadingEdge

                val targetForLeadingEdge =
                    if (childSmallerThanParent && spaceAvailableToShowItem < size) {
                        containerSize - size
                    } else {
                        initialTargetForLeadingEdge
                    }

                return offset - targetForLeadingEdge
            }
        }
    }

    // LocalBringIntoViewSpec will apply to all scrollables in the hierarchy.
    CompositionLocalProvider(
        LocalBringIntoViewSpec provides bringIntoViewSpec,
        content = content,
    )
}
