@file:OptIn(ExperimentalMaterial3Api::class)

package tv.trakt.trakt.core.movies.ui.context.sheet

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
import org.koin.core.parameter.parametersOf
import tv.trakt.trakt.LocalSnackbarState
import tv.trakt.trakt.common.model.Movie
import tv.trakt.trakt.core.movies.ui.context.MovieContextView
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.components.TraktBottomSheet
import tv.trakt.trakt.ui.snackbar.SNACK_DURATION_SHORT
import kotlin.random.Random.Default.nextInt

@Composable
internal fun MovieContextSheet(
    state: SheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true,
    ),
    movie: Movie?,
    onDismiss: () -> Unit,
) {
    val sheetScope = rememberCoroutineScope()

    if (movie != null) {
        val localSnack = LocalSnackbarState.current
        val localContext = LocalContext.current

        TraktBottomSheet(
            sheetState = state,
            onDismiss = onDismiss,
        ) {
            MovieContextView(
                movie = movie,
                viewModel = koinViewModel(
                    key = nextInt().toString(),
                    parameters = { parametersOf(movie) },
                ),
                onAddWatched = {
                    sheetScope.run {
                        launch { state.hide() }
                            .invokeOnCompletion {
                                if (!state.isVisible) {
                                    onDismiss()
                                }
                            }
                        launch {
                            val job = sheetScope.launch {
                                localSnack.showSnackbar(localContext.getString(R.string.text_info_history_added))
                            }
                            delay(SNACK_DURATION_SHORT)
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
                        launch {
                            val job = sheetScope.launch {
                                localSnack.showSnackbar(localContext.getString(R.string.text_info_watchlist_added))
                            }
                            delay(SNACK_DURATION_SHORT)
                            job.cancel()
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
                                localSnack.showSnackbar(localContext.getString(R.string.text_info_watchlist_removed))
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
                                    localContext.getString(R.string.error_text_unexpected_error_short),
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
