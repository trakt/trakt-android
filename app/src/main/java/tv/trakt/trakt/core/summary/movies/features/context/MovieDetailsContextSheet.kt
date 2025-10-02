@file:OptIn(ExperimentalMaterial3Api::class)

package tv.trakt.trakt.core.summary.movies.features.context

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SheetState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import tv.trakt.trakt.common.model.Movie
import tv.trakt.trakt.ui.components.TraktBottomSheet

@Composable
internal fun MovieDetailsContextSheet(
    state: SheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true,
    ),
    movie: Movie?,
    onShareClick: (() -> Unit)? = null,
    onTrailerClick: (() -> Unit)? = null,
    onDismiss: () -> Unit,
) {
    val sheetScope = rememberCoroutineScope()

    if (movie != null) {
        TraktBottomSheet(
            sheetState = state,
            onDismiss = onDismiss,
        ) {
            MovieDetailsContextView(
                movie = movie,
                onShareClick = {
                    onShareClick?.invoke()
                    sheetScope.launch { state.hide() }
                        .invokeOnCompletion {
                            if (!state.isVisible) {
                                onDismiss()
                            }
                        }
                },
                onTrailerClick = {
                    onTrailerClick?.invoke()
                    sheetScope.launch { state.hide() }
                        .invokeOnCompletion {
                            if (!state.isVisible) {
                                onDismiss()
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
