@file:OptIn(ExperimentalMaterial3Api::class)

package tv.trakt.trakt.core.summary.shows.features.context.history

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SheetState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import tv.trakt.trakt.core.home.sections.activity.model.HomeActivityItem
import tv.trakt.trakt.ui.components.TraktBottomSheet

@Composable
internal fun ShowDetailsHistorySheet(
    state: SheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true,
    ),
    sheetItem: HomeActivityItem.EpisodeItem?,
    onRemovePlay: (HomeActivityItem.EpisodeItem) -> Unit,
    onDismiss: () -> Unit,
) {
    val sheetScope = rememberCoroutineScope()

    if (sheetItem != null) {
        TraktBottomSheet(
            sheetState = state,
            onDismiss = onDismiss,
        ) {
            ShowDetailsHistoryView(
                item = sheetItem,
                onRemovePlayClick = {
                    sheetScope.run {
                        launch { state.hide() }
                            .invokeOnCompletion {
                                if (!state.isVisible) {
                                    onRemovePlay(sheetItem)
                                    onDismiss()
                                }
                            }
                    }
                },
                modifier = Modifier
                    .padding(bottom = 24.dp)
                    .padding(horizontal = 24.dp),
            )
        }
    }
}
