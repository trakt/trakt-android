@file:OptIn(ExperimentalMaterial3Api::class)

package tv.trakt.trakt.core.home.sections.upnext.features.context.sheets

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SheetState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel
import tv.trakt.trakt.LocalSnackbarState
import tv.trakt.trakt.common.core.home.model.UpNextItem
import tv.trakt.trakt.common.core.home.model.UpNextMovie
import tv.trakt.trakt.common.core.home.model.UpNextShow
import tv.trakt.trakt.core.home.sections.upnext.features.context.UpNextItemContextView
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.components.TraktBottomSheet
import tv.trakt.trakt.ui.snackbar.SNACK_DURATION_SHORT
import kotlin.random.Random.Default.nextInt

@Composable
internal fun UpNextItemContextSheet(
    state: SheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true,
    ),
    sheetItem: UpNextItem?,
    onAddWatched: (UpNextItem) -> Unit,
    onDropped: (UpNextItem) -> Unit,
    onDismiss: () -> Unit,
) {
    val localSnack = LocalSnackbarState.current
    val localRes = LocalResources.current

    val sheetScope = rememberCoroutineScope()

    if (sheetItem != null) {
        TraktBottomSheet(
            sheetState = state,
            onDismiss = onDismiss,
        ) {
            UpNextItemContextView(
                item = sheetItem,
                viewModel = koinViewModel(
                    key = nextInt().toString(),
                ),
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
                onDropped = { item ->
                    onDropped(item)
                    sheetScope.run {
                        launch { state.hide() }
                            .invokeOnCompletion {
                                if (!state.isVisible) {
                                    onDismiss()
                                }
                            }
                        launch {
                            val job = sheetScope.launch {
                                localSnack.showSnackbar(
                                    localRes.getString(
                                        when (item) {
                                            is UpNextMovie -> R.string.text_info_movie_dropped
                                            is UpNextShow -> R.string.text_info_show_dropped
                                        },
                                    ),
                                )
                            }
                            delay(SNACK_DURATION_SHORT)
                            job.cancel()
                        }
                    }
                },
                onError = {
                    sheetScope.run {
                        launch { state.hide() }
                            .invokeOnCompletion {
                                if (!state.isVisible) {
                                    onDismiss()
                                }
                            }
                        launch {
                            val job = sheetScope.launch {
                                localSnack.showSnackbar(
                                    localRes.getString(R.string.error_text_unexpected_error_short),
                                )
                            }
                            delay(SNACK_DURATION_SHORT)
                            job.cancel()
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
