@file:OptIn(ExperimentalMaterial3Api::class)

package tv.trakt.trakt.core.summary.shows.features.context.seasons

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SheetState
import androidx.compose.material3.SheetValue.Expanded
import androidx.compose.material3.SheetValue.Hidden
import androidx.compose.material3.rememberBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import tv.trakt.trakt.core.summary.shows.features.seasons.model.SeasonItem
import tv.trakt.trakt.ui.components.TraktBottomSheet

@Composable
internal fun SeasonContextSheet(
    state: SheetState = rememberBottomSheetState(
        initialValue = Hidden,
        enabledValues = setOf(Hidden, Expanded),
    ),
    seasonItem: SeasonItem?,
    showTitle: String?,
    watchOnlyOnce: Boolean?,
    onTrackClick: (SeasonItem) -> Unit,
    onRemoveClick: (SeasonItem) -> Unit,
    onDismiss: () -> Unit,
) {
    val scope = rememberCoroutineScope()
    if (seasonItem != null) {
        TraktBottomSheet(
            sheetState = state,
            onDismiss = onDismiss,
        ) {
            SeasonContextView(
                season = seasonItem.season,
                watched = seasonItem.isWatched,
                watchOnlyOnce = watchOnlyOnce,
                showTitle = showTitle,
                onTrackClick = {
                    onTrackClick(seasonItem)
                    scope.launch {
                        state.hide()
                        onDismiss()
                    }
                },
                onRemoveClick = {
                    onRemoveClick(seasonItem)
                    scope.launch {
                        state.hide()
                        onDismiss()
                    }
                },
                modifier = Modifier
                    .padding(bottom = 24.dp)
                    .padding(horizontal = 24.dp),
            )
        }
    }
}
