@file:OptIn(ExperimentalMaterial3Api::class)

package tv.trakt.trakt.core.summary.episodes.features.info

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SheetState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf
import tv.trakt.trakt.common.model.Episode
import tv.trakt.trakt.common.model.Show
import tv.trakt.trakt.ui.components.TraktBottomSheet
import kotlin.random.Random.Default.nextInt

@Composable
internal fun EpisodeInfoSheet(
    state: SheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true,
    ),
    show: Show?,
    episode: Episode?,
    onDismiss: () -> Unit,
) {
    if (show != null && episode != null) {
        TraktBottomSheet(
            sheetState = state,
            onDismiss = onDismiss,
        ) {
            EpisodeInfoView(
                viewModel = koinViewModel(
                    key = nextInt().toString(),
                    parameters = { parametersOf(show, episode) },
                ),
                modifier = Modifier
                    .padding(bottom = 24.dp),
            )
        }
    }
}
