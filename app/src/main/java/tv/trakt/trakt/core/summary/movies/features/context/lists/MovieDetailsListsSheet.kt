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
import kotlinx.collections.immutable.ImmutableList
import kotlinx.coroutines.launch
import tv.trakt.trakt.common.model.CustomList
import tv.trakt.trakt.common.model.Movie
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.ui.components.TraktBottomSheet

@Composable
internal fun MovieDetailsListsSheet(
    state: SheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true,
    ),
    movie: Movie?,
    inWatchlist: Boolean,
    lists: ImmutableList<Pair<CustomList, Boolean>>,
    onWatchlistClick: (() -> Unit)? = null,
    onListClick: ((TraktId) -> Unit)? = null,
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
                inWatchlist = inWatchlist,
                lists = lists,
                onWatchlistClick = {
                    onWatchlistClick?.invoke()
                    sheetScope.launch { state.hide() }
                        .invokeOnCompletion {
                            if (!state.isVisible) {
                                onDismiss()
                            }
                        }
                },
                onListClick = { listId: TraktId ->
                    onListClick?.invoke(listId)
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
