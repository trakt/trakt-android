@file:OptIn(ExperimentalMaterial3Api::class)

package tv.trakt.trakt.core.home.sections.upnext.features.context.sheets

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
import tv.trakt.trakt.common.core.home.model.UpNextItem
import tv.trakt.trakt.common.core.home.model.UpNextMovie
import tv.trakt.trakt.common.core.home.model.UpNextShow
import tv.trakt.trakt.common.model.MediaType
import tv.trakt.trakt.common.model.NoteType
import tv.trakt.trakt.core.home.sections.upnext.features.context.UpNextItemContextView
import tv.trakt.trakt.core.notes.ui.AddNoteSheet
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.components.TraktBottomSheet
import tv.trakt.trakt.ui.snackbar.ShortSnackDuration
import tv.trakt.trakt.ui.snackbar.showNotePrompt
import kotlin.random.Random.Default.nextInt

@Composable
internal fun UpNextItemContextSheet(
    state: SheetState = rememberBottomSheetState(
        initialValue = Hidden,
        enabledValues = setOf(Hidden, Expanded),
    ),
    sheetItem: UpNextItem?,
    onAddWatched: (UpNextItem) -> Unit,
    onDropped: (UpNextItem) -> Unit,
    onDismiss: () -> Unit,
) {
    val localSnack = LocalSnackbarState.current
    val localRes = LocalResources.current

    val sheetScope = rememberCoroutineScope()
    val viewModelKey = remember(sheetItem) { nextInt().toString() }

    var noteItem by remember { mutableStateOf<UpNextItem?>(null) }
    val noteViewModelKey = remember(noteItem) { nextInt().toString() }

    if (sheetItem != null) {
        TraktBottomSheet(
            sheetState = state,
            onDismiss = onDismiss,
        ) {
            UpNextItemContextView(
                item = sheetItem,
                viewModel = koinViewModel(
                    key = viewModelKey,
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
                            localSnack.showNotePrompt(
                                resources = localRes,
                                message = when (item) {
                                    is UpNextMovie -> R.string.text_info_movie_dropped
                                    is UpNextShow -> R.string.text_info_show_dropped
                                },
                                onAddNote = { noteItem = item },
                            )
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

    noteItem?.let { item ->
        AddNoteSheet(
            viewModel = koinViewModel(
                key = noteViewModelKey,
                parameters = {
                    parametersOf(
                        when (item) {
                            is UpNextMovie -> MediaType.Movie
                            is UpNextShow -> MediaType.Show
                        },
                        item.mediaId,
                    )
                },
            ),
            type = NoteType.Note,
            title = when (item) {
                is UpNextMovie -> item.movie.title
                is UpNextShow -> item.show.title
            },
            onDismiss = { noteItem = null },
        )
    }
}
