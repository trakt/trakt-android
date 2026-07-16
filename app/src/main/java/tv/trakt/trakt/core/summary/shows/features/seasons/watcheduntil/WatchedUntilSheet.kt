@file:OptIn(ExperimentalMaterial3Api::class)

package tv.trakt.trakt.core.summary.shows.features.seasons.watcheduntil

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SheetState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf
import tv.trakt.trakt.common.model.Episode
import tv.trakt.trakt.common.model.Show
import tv.trakt.trakt.ui.components.TraktBottomSheet
import kotlin.random.Random.Default.nextInt

@Composable
internal fun WatchedUntilSheet(
    state: SheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true,
    ),
    show: Show?,
    episode: Episode?,
    onMarkAsWatched: () -> Unit = {},
    onDismiss: () -> Unit,
) {
    val scope = rememberCoroutineScope()

    if (show != null && episode != null) {
        TraktBottomSheet(
            sheetState = state,
            onDismiss = onDismiss,
        ) {
            WatchedUntilView(
                viewModel = koinViewModel(
                    key = nextInt().toString(),
                    parameters = { parametersOf(show, episode) },
                ),
                onMarkAsWatched = onMarkAsWatched,
                onDismiss = {
                    scope.run {
                        launch { state.hide() }
                            .invokeOnCompletion {
                                if (!state.isVisible) {
                                    onDismiss()
                                }
                            }
                    }
                },
            )
        }
    }
}
