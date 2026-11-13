@file:OptIn(ExperimentalMaterial3Api::class)

package tv.trakt.trakt.core.home.sections.activity.sheets

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SheetState
import androidx.compose.material3.SheetValue.Expanded
import androidx.compose.material3.SheetValue.Hidden
import androidx.compose.material3.rememberBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf
import tv.trakt.trakt.LocalSnackbarState
import tv.trakt.trakt.common.model.activity.HomeActivityItem
import tv.trakt.trakt.core.home.sections.activity.views.context.ActivityItemContextView
import tv.trakt.trakt.core.lists.features.manage.ManageListsSheet
import tv.trakt.trakt.core.lists.features.manage.ManageListsTarget
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.components.TraktBottomSheet
import tv.trakt.trakt.ui.snackbar.ShortSnackDuration
import tv.trakt.trakt.ui.snackbar.showWatchlistAdded
import kotlin.random.Random.Default.nextInt

@Composable
internal fun HomeActivityItemSheet(
    state: SheetState = rememberBottomSheetState(
        initialValue = Hidden,
        enabledValues = setOf(Hidden, Expanded),
    ),
    sheetItem: HomeActivityItem?,
    onPlayRemoved: (HomeActivityItem) -> Unit,
    onDismiss: () -> Unit,
) {
    val localSnack = LocalSnackbarState.current
    val localRes = LocalResources.current

    val sheetScope = rememberCoroutineScope()
    val viewModelKey = remember(sheetItem) { nextInt().toString() }
    var listsTarget by remember { mutableStateOf<ManageListsTarget?>(null) }

    if (sheetItem != null) {
        TraktBottomSheet(
            sheetState = state,
            onDismiss = onDismiss,
        ) {
            ActivityItemContextView(
                item = sheetItem,
                viewModel = koinViewModel(
                    key = viewModelKey,
                    parameters = { parametersOf(sheetItem) },
                ),
                onPlayRemove = {
                    onPlayRemoved(sheetItem)
                    sheetScope.run {
                        launch { state.hide() }
                            .invokeOnCompletion {
                                if (!state.isVisible) {
                                    onDismiss()
                                }
                            }
                        launch {
                            val job = sheetScope.launch {
                                localSnack.showSnackbar(localRes.getString(R.string.text_info_history_removed))
                            }
                            delay(ShortSnackDuration)
                            job.cancel()
                        }
                    }
                },
                onAddWatchlist = {
                    sheetScope.run {
                        launch { state.hide() }
                            .invokeOnCompletion {
                                if (!state.isVisible) {
                                    onDismiss()
                                }
                            }
                        val movie = (sheetItem as? HomeActivityItem.MovieItem)?.movie ?: return@run
                        launch {
                            localSnack.showWatchlistAdded(
                                resources = localRes,
                                title = movie.title,
                                onChangeList = { listsTarget = ManageListsTarget.MovieTarget(movie) },
                            )
                        }
                    }
                },
                onRemoveWatchlist = {
                    sheetScope.run {
                        launch { state.hide() }
                            .invokeOnCompletion {
                                if (!state.isVisible) {
                                    onDismiss()
                                }
                            }
                        launch {
                            val job = sheetScope.launch {
                                localSnack.showSnackbar(localRes.getString(R.string.text_info_watchlist_removed))
                            }
                            delay(ShortSnackDuration)
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
                            delay(ShortSnackDuration)
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

    ManageListsSheet(
        target = listsTarget,
        onDismiss = { listsTarget = null },
    )
}
