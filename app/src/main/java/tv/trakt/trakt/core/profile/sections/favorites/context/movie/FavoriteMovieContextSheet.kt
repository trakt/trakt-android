@file:OptIn(ExperimentalMaterial3Api::class)

package tv.trakt.trakt.core.profile.sections.favorites.context.movie

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
import tv.trakt.trakt.common.model.Movie
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.components.TraktBottomSheet
import tv.trakt.trakt.ui.snackbar.SNACK_DURATION_SHORT
import kotlin.random.Random.Default.nextInt

@Composable
internal fun FavoriteMovieContextSheet(
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
            FavoriteMovieContextView(
                movie = movie,
                viewModel = koinViewModel(
                    key = nextInt().toString(),
                    parameters = { parametersOf(movie) },
                ),
                onRemovedFromFavorites = {
                    sheetScope.dismissWithMessage(
                        state = state,
                        snackHost = localSnack,
                        onDismiss = onDismiss,
                        message = localContext.getString(R.string.text_info_favorites_removed),
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
