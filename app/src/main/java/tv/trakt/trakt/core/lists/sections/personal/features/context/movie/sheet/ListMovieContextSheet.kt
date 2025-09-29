@file:OptIn(ExperimentalMaterial3Api::class)

package tv.trakt.trakt.core.lists.sections.personal.features.context.movie.sheet

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SheetState
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf
import tv.trakt.trakt.LocalSnackbarState
import tv.trakt.trakt.common.model.CustomList
import tv.trakt.trakt.common.model.Movie
import tv.trakt.trakt.core.lists.sections.personal.features.context.movie.ListMovieContextView
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.components.TraktBottomSheet
import tv.trakt.trakt.ui.snackbar.SNACK_DURATION_SHORT
import kotlin.random.Random.Default.nextInt

@Composable
internal fun ListMovieContextSheet(
    state: SheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true,
    ),
    movie: Movie?,
    list: CustomList?,
    onRemoveListItem: (() -> Unit)? = null,
    onDismiss: () -> Unit,
) {
    val sheetScope = rememberCoroutineScope()

    if (movie != null && list != null) {
        val localSnack = LocalSnackbarState.current
        val localContext = LocalContext.current

        TraktBottomSheet(
            sheetState = state,
            onDismiss = onDismiss,
        ) {
            ListMovieContextView(
                movie = movie,
                list = list,
                viewModel = koinViewModel(
                    key = nextInt().toString(),
                    parameters = { parametersOf(movie, list) },
                ),
                onAddWatched = {
                    sheetScope.dismissWithMessage(
                        state = state,
                        snackHost = localSnack,
                        onDismiss = onDismiss,
                        message = localContext.getString(R.string.text_info_history_added),
                    )
                },
                onAddWatchlist = {
                    sheetScope.dismissWithMessage(
                        state = state,
                        snackHost = localSnack,
                        onDismiss = onDismiss,
                        message = localContext.getString(R.string.text_info_watchlist_added),
                    )
                },
                onRemoveWatched = {
                    sheetScope.dismissWithMessage(
                        state = state,
                        snackHost = localSnack,
                        onDismiss = onDismiss,
                        message = localContext.getString(R.string.text_info_history_removed),
                    )
                },
                onRemoveWatchlist = {
                    sheetScope.dismissWithMessage(
                        state = state,
                        snackHost = localSnack,
                        onDismiss = onDismiss,
                        message = localContext.getString(R.string.text_info_watchlist_removed),
                    )
                },
                onRemoveList = {
                    onRemoveListItem?.invoke()
                    sheetScope.dismissWithMessage(
                        state = state,
                        snackHost = localSnack,
                        onDismiss = onDismiss,
                        message = localContext.getString(R.string.text_info_list_removed),
                    )
                },
                onError = {
                    sheetScope.dismissWithMessage(
                        state = state,
                        snackHost = localSnack,
                        onDismiss = onDismiss,
                        message = localContext.getString(R.string.error_text_unexpected_error_short),
                    )
                },
                modifier = Modifier
                    .padding(bottom = 24.dp)
                    .padding(horizontal = 24.dp),
            )
        }
    }
}

private fun CoroutineScope.dismissWithMessage(
    state: SheetState,
    onDismiss: () -> Unit,
    snackHost: SnackbarHostState,
    message: String,
) {
    launch { state.hide() }
        .invokeOnCompletion {
            if (!state.isVisible) {
                onDismiss()
            }
        }
    launch {
        val job = this@dismissWithMessage.launch {
            snackHost.showSnackbar(message)
        }
        delay(SNACK_DURATION_SHORT)
        job.cancel()
    }
}
