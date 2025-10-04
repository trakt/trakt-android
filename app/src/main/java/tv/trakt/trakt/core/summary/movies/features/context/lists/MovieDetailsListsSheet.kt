@file:OptIn(ExperimentalMaterial3Api::class)

package tv.trakt.trakt.core.summary.movies.features.context.lists

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SheetState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf
import tv.trakt.trakt.common.model.Movie
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.ui.components.TraktBottomSheet
import kotlin.random.Random.Default.nextInt

@Composable
internal fun MovieDetailsListsSheet(
    state: SheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true,
    ),
    movie: Movie?,
    inWatchlist: Boolean,
    onWatchlistClick: (() -> Unit)? = null,
    onAddListClick: ((TraktId) -> Unit)? = null,
    onRemoveListClick: ((TraktId) -> Unit)? = null,
    onDismiss: () -> Unit,
) {
    val sheetScope = rememberCoroutineScope()

    if (movie != null) {
        TraktBottomSheet(
            sheetState = state,
            onDismiss = onDismiss,
        ) {
            MovieDetailsListsView(
                movie = movie,
                viewModel = koinViewModel(
                    key = nextInt().toString(),
                    parameters = { parametersOf(movie) },
                ),
                inWatchlist = inWatchlist,
                onWatchlistClick = {
                    onWatchlistClick?.invoke()
                    sheetScope.launch {
                        state.hide()
                        onDismiss()
                    }
                },
                onAddListClick = { listId: TraktId ->
                    onAddListClick?.invoke(listId)
                    sheetScope.launch {
                        state.hide()
                        onDismiss()
                    }
                },
                onRemoveListClick = { listId: TraktId ->
                    onRemoveListClick?.invoke(listId)
                    sheetScope.launch {
                        state.hide()
                        onDismiss()
                    }
                },
                modifier = Modifier
                    .padding(bottom = 24.dp)
                    .padding(horizontal = 24.dp),
            )
        }
    }
}
