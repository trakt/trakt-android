@file:OptIn(ExperimentalMaterial3Api::class)

package tv.trakt.trakt.core.summary.shows.features.context.episodes

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SheetState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import tv.trakt.trakt.core.summary.shows.features.seasons.model.EpisodeItem
import tv.trakt.trakt.ui.components.TraktBottomSheet

@Composable
internal fun EpisodeContextSheet(
    state: SheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true,
    ),
    episodeItem: EpisodeItem?,
    onTrackClick: (EpisodeItem) -> Unit,
    onWatchedUntilClick: (EpisodeItem) -> Unit,
    onRemoveClick: (EpisodeItem) -> Unit,
    onDismiss: () -> Unit,
) {
    val scope = rememberCoroutineScope()
    if (episodeItem != null) {
        TraktBottomSheet(
            sheetState = state,
            onDismiss = onDismiss,
        ) {
            EpisodeContextView(
                episode = episodeItem.episode,
                watched = episodeItem.isWatched,
                onTrackClick = {
                    onTrackClick(episodeItem)
                    scope.launch {
                        state.hide()
                        onDismiss()
                    }
                },
                onWatchedUntilClick = {
                    onWatchedUntilClick(episodeItem)
                    scope.launch {
                        state.hide()
                        onDismiss()
                    }
                },
                onRemoveClick = {
                    onRemoveClick(episodeItem)
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
