@file:OptIn(ExperimentalMaterial3Api::class)

package tv.trakt.trakt.core.summary.episodes.features.context.more

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
import tv.trakt.trakt.common.model.Episode
import tv.trakt.trakt.common.model.Show
import tv.trakt.trakt.ui.components.TraktBottomSheet
import kotlin.random.Random.Default.nextInt

@Composable
internal fun EpisodeDetailsContextSheet(
    state: SheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true,
    ),
    active: Boolean,
    show: Show?,
    episode: Episode?,
    watched: Boolean,
    onCheckClick: (() -> Unit)? = null,
    onRemoveClick: (() -> Unit)? = null,
    onShareClick: (() -> Unit)? = null,
    onDismiss: () -> Unit,
) {
    val sheetScope = rememberCoroutineScope()

    if (active && show != null && episode != null) {
        TraktBottomSheet(
            sheetState = state,
            onDismiss = onDismiss,
        ) {
            EpisodeDetailsContextView(
                episode = episode,
                watched = watched,
                viewModel = koinViewModel(
                    key = nextInt().toString(),
                    parameters = { parametersOf(show, episode) },
                ),
                onCheckClick = {
                    onCheckClick?.invoke()
                    sheetScope.launch { state.hide() }
                        .invokeOnCompletion {
                            if (!state.isVisible) {
                                onDismiss()
                            }
                        }
                },
                onRemoveClick = {
                    onRemoveClick?.invoke()
                    sheetScope.launch { state.hide() }
                        .invokeOnCompletion {
                            if (!state.isVisible) {
                                onDismiss()
                            }
                        }
                },
                onShareClick = {
                    onShareClick?.invoke()
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
