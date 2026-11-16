@file:OptIn(ExperimentalMaterial3Api::class)

package tv.trakt.trakt.core.notes.ui

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import tv.trakt.trakt.common.model.NoteType
import tv.trakt.trakt.core.notes.NotesViewModel

@Composable
internal fun AddNoteSheet(
    viewModel: NotesViewModel,
    type: NoteType,
    title: String,
    onDismiss: () -> Unit,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var opened by remember(viewModel) { mutableStateOf(false) }

    LaunchedEffect(viewModel) {
        viewModel.addNote(type)
    }

    LaunchedEffect(state.editor) {
        when {
            state.editor != null -> opened = true
            opened -> onDismiss()
        }
    }

    NoteEditorSheet(
        editor = state.editor,
        title = title,
        loading = state.loadingEditor.isLoading,
        error = state.error,
        onSubmitClick = viewModel::submitNote,
        onErrorClick = viewModel::clearError,
        onDismiss = viewModel::closeEditor,
    )
}
