@file:OptIn(ExperimentalMaterial3Api::class)

package tv.trakt.trakt.core.summary.shows.features.progress

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SheetState
import androidx.compose.material3.SheetValue.Expanded
import androidx.compose.material3.SheetValue.Hidden
import androidx.compose.material3.rememberBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalWindowInfo
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf
import tv.trakt.trakt.common.model.Show
import tv.trakt.trakt.ui.components.TraktBottomSheet
import kotlin.math.roundToInt
import kotlin.random.Random.Default.nextInt

private const val MAX_HEIGHT_FRACTION = 0.75F

@Composable
internal fun ShowProgressSheet(
    show: Show?,
    state: SheetState = rememberBottomSheetState(
        initialValue = Hidden,
        enabledValues = setOf(Hidden, Expanded),
    ),
    onMarkWatchedClick: () -> Unit,
    onRemoveWatchedClick: () -> Unit,
    onDismiss: () -> Unit,
) {
    val viewModelKey = remember(show) {
        nextInt().toString()
    }

    val windowHeight = LocalWindowInfo.current.containerSize.height

    if (show != null) {
        TraktBottomSheet(
            sheetState = state,
            onDismiss = onDismiss,
        ) {
            ShowProgressView(
                viewModel = koinViewModel(
                    key = viewModelKey,
                    parameters = { parametersOf(show) },
                ),
                onMarkWatchedClick = onMarkWatchedClick,
                onRemoveWatchedClick = onRemoveWatchedClick,
                modifier = Modifier.sheetFixedHeight(windowHeight),
            )
        }
    }
}

// The sheet anchors against its content height, so any height change after open makes it slide
// out of place and back. The content always takes the full allowed height instead, which keeps the
// anchor constant while data loads. A max height on the sheet modifier would also shrink the area
// ModalBottomSheet anchors against, leaving a gap below, so the window share is reserved here.
private fun Modifier.sheetFixedHeight(windowHeight: Int): Modifier {
    return layout { measurable, constraints ->
        if (!constraints.hasBoundedHeight) {
            val placeable = measurable.measure(constraints)
            return@layout layout(placeable.width, placeable.height) {
                placeable.place(0, 0)
            }
        }

        val reserved = (windowHeight * (1F - MAX_HEIGHT_FRACTION)).roundToInt()
        val height = (constraints.maxHeight - reserved).coerceAtLeast(constraints.minHeight)
        val placeable = measurable.measure(
            constraints.copy(minHeight = height, maxHeight = height),
        )

        layout(placeable.width, placeable.height) {
            placeable.place(0, 0)
        }
    }
}
