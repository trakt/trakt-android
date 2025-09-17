@file:OptIn(ExperimentalMaterial3Api::class)

package tv.trakt.trakt.core.home.sections.watchlist.sheets

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SheetState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel
import tv.trakt.trakt.LocalSnackbarState
import tv.trakt.trakt.common.model.Movie
import tv.trakt.trakt.core.home.sections.watchlist.views.WatchlistItemContextView
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.components.TraktBottomSheet
import tv.trakt.trakt.ui.snackbar.SNACK_DURATION_SHORT
import kotlin.random.Random.Default.nextInt

@Composable
internal fun HomeWatchlistItemSheet(
    state: SheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true,
    ),
    sheetItem: Movie?,
    onAddWatched: (Movie) -> Unit,
    onRemoveWatchlist: () -> Unit,
    onDismiss: () -> Unit,
) {
    val sheetScope = rememberCoroutineScope()

    if (sheetItem != null) {
        val localSnack = LocalSnackbarState.current
        val localContext = LocalContext.current

        TraktBottomSheet(
            sheetState = state,
            onDismiss = onDismiss,
        ) {
            WatchlistItemContextView(
                item = sheetItem,
                viewModel = koinViewModel(
                    key = nextInt().toString(),
                ),
                onRemoveWatchlist = {
                    onRemoveWatchlist()
                    sheetScope.run {
                        launch { state.hide() }
                            .invokeOnCompletion {
                                if (!state.isVisible) {
                                    onDismiss()
                                }
                            }
                        launch {
                            val job = sheetScope.launch {
                                localSnack.showSnackbar(localContext.getString(R.string.text_info_watchlist_removed))
                            }
                            delay(SNACK_DURATION_SHORT)
                            job.cancel()
                        }
                    }
                },
                onAddWatched = {
                    onAddWatched(it)
                    sheetScope.run {
                        launch { state.hide() }
                            .invokeOnCompletion {
                                if (!state.isVisible) {
                                    onDismiss()
                                }
                            }
                    }
                },
                modifier = Modifier
                    .padding(top = 4.dp)
                    .padding(bottom = 24.dp)
                    .padding(horizontal = 24.dp),
            )
        }
    }
}
