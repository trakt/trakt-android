@file:OptIn(ExperimentalMaterial3Api::class)

package tv.trakt.trakt.core.lists.sections.watchlist.features.context.movies.sheets

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
import tv.trakt.trakt.LocalSnackbarState
import tv.trakt.trakt.common.model.Movie
import tv.trakt.trakt.core.lists.features.manage.ManageListsSheet
import tv.trakt.trakt.core.lists.features.manage.ManageListsTarget
import tv.trakt.trakt.core.lists.sections.watchlist.features.context.movies.WatchlistMovieContextView
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.components.TraktBottomSheet
import tv.trakt.trakt.ui.snackbar.ShortSnackDuration
import kotlin.random.Random.Default.nextInt

@Composable
internal fun WatchlistMovieSheet(
    state: SheetState = rememberBottomSheetState(
        initialValue = Hidden,
        enabledValues = setOf(Hidden, Expanded),
    ),
    movie: Movie?,
    watched: Boolean,
    addLocally: Boolean,
    skipSnack: Boolean = false,
    onAddWatched: (Movie) -> Unit,
    onRemoveWatchlist: () -> Unit,
    onDismiss: () -> Unit,
) {
    val sheetScope = rememberCoroutineScope()
    val viewModelKey = remember(movie) { nextInt().toString() }
    var listsTarget by remember { mutableStateOf<ManageListsTarget?>(null) }

    if (movie != null) {
        val localSnack = LocalSnackbarState.current
        val localRes = LocalResources.current

        TraktBottomSheet(
            sheetState = state,
            onDismiss = onDismiss,
        ) {
            WatchlistMovieContextView(
                item = movie,
                watched = watched,
                addLocally = addLocally,
                viewModel = koinViewModel(
                    key = viewModelKey,
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
                                localSnack.showSnackbar(localRes.getString(R.string.text_info_watchlist_removed))
                            }
                            delay(ShortSnackDuration)
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
                        if (addLocally && !skipSnack) {
                            launch {
                                val job = sheetScope.launch {
                                    localSnack.showSnackbar(
                                        localRes.getString(R.string.text_info_history_added),
                                    )
                                }
                                delay(ShortSnackDuration)
                                job.cancel()
                            }
                        }
                    }
                },
                onCheckIn = {
                    sheetScope.launch { state.hide() }
                        .invokeOnCompletion {
                            if (!state.isVisible) {
                                onDismiss()
                            }
                        }
                },
                onListsClick = {
                    listsTarget = ManageListsTarget.MovieTarget(movie)
                    sheetScope.launch { state.hide() }
                        .invokeOnCompletion {
                            if (!state.isVisible) {
                                onDismiss()
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
