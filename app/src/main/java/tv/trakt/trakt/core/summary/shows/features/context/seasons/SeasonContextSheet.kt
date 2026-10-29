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
import tv.trakt.trakt.common.model.Season
import tv.trakt.trakt.common.model.Show
import tv.trakt.trakt.ui.components.TraktBottomSheet

@Composable
internal fun SeasonContextSheet(
    state: SheetState = rememberBottomSheetState(
        initialValue = Hidden,
        enabledValues = setOf(Hidden, Expanded),
    ),
    show: Show?,
    season: Season?,
    watched: Boolean,
    onTrackClick: () -> Unit,
    onRemoveClick: () -> Unit,
    onListsClick: (() -> Unit)? = null,
    onDismiss: () -> Unit,
) {
    val scope = rememberCoroutineScope()

    fun dismissAfter(action: () -> Unit): () -> Unit =
        {
            action()
            scope.launch {
                state.hide()
                onDismiss()
            }
        }

    if (show != null && season != null) {
        TraktBottomSheet(
            sheetState = state,
            onDismiss = onDismiss,
        ) {
            SeasonContextView(
                show = show,
                season = season,
                watched = watched,
                onTrackClick = dismissAfter(onTrackClick),
                onRemoveClick = dismissAfter(onRemoveClick),
                onListsClick = onListsClick?.let(::dismissAfter),
                modifier = Modifier
                    .padding(bottom = 24.dp)
                    .padding(horizontal = 24.dp),
            )
        }
    }
}
