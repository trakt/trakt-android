@file:OptIn(ExperimentalMaterial3Api::class)

package tv.trakt.trakt.core.notes.ui

import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SheetState
import androidx.compose.material3.SheetValue.Expanded
import androidx.compose.material3.SheetValue.Hidden
import androidx.compose.material3.Text
import androidx.compose.material3.rememberBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow.Companion.Ellipsis
import androidx.compose.ui.unit.dp
import tv.trakt.trakt.common.helpers.extensions.onClick
import tv.trakt.trakt.common.model.NoteType
import tv.trakt.trakt.common.ui.theme.colors.Red400
import tv.trakt.trakt.common.ui.theme.colors.Red500
import tv.trakt.trakt.core.notes.NOTE_CHARACTER_LIMIT
import tv.trakt.trakt.core.notes.NotesState.NoteEditor
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.components.InputField
import tv.trakt.trakt.ui.components.TraktBottomSheet
import tv.trakt.trakt.ui.components.TraktHeader
import tv.trakt.trakt.ui.components.buttons.PrimaryButton
import tv.trakt.trakt.ui.theme.TraktTheme

@Composable
internal fun NoteEditorSheet(
    state: SheetState = rememberBottomSheetState(
        initialValue = Hidden,
        enabledValues = setOf(Hidden, Expanded),
    ),
    editor: NoteEditor?,
    title: String,
    loading: Boolean,
    error: Exception?,
    onSubmitClick: (String) -> Unit,
    onErrorClick: () -> Unit,
    onDismiss: () -> Unit,
) {
    if (editor != null) {
        TraktBottomSheet(
            sheetState = state,
            onDismiss = onDismiss,
        ) {
            NoteEditorView(
                editor = editor,
                title = title,
                loading = loading,
                error = error,
                onSubmitClick = onSubmitClick,
                onErrorClick = onErrorClick,
                modifier = Modifier
                    .padding(horizontal = 24.dp)
                    .padding(bottom = 24.dp),
            )
        }
    }
}

@Composable
private fun NoteEditorView(
    editor: NoteEditor,
    title: String,
    loading: Boolean,
    error: Exception?,
    onSubmitClick: (String) -> Unit,
    onErrorClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val initialText = editor.note?.text.orEmpty()
    val textState = rememberTextFieldState(initialText)
    val focusRequester = remember { FocusRequester() }

    val trimmed = textState.text.trim().toString()
    val isTooLong = trimmed.length > NOTE_CHARACTER_LIMIT
    val isValid = trimmed.isNotEmpty() && !isTooLong
    val isChanged = trimmed != initialText.trim()

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    Column(
        verticalArrangement = spacedBy(8.dp),
        modifier = modifier,
    ) {
        TraktHeader(
            title = stringResource(R.string.drawer_title_add_note),
            subtitle = title,
            modifier = Modifier.padding(bottom = 8.dp),
        )

        InputField(
            state = textState,
            enabled = !loading,
            placeholder = stringResource(
                when (editor.type) {
                    NoteType.Favorites -> R.string.input_placeholder_note_favorite
                    NoteType.Note -> R.string.input_placeholder_note_drop
                },
            ),
            containerColor = Color.Transparent,
            borderColor = if (isTooLong) Red400 else TraktTheme.colors.accent,
            lineLimits = TextFieldLineLimits.MultiLine(
                minHeightInLines = 5,
                maxHeightInLines = 10,
            ),
            modifier = Modifier
                .fillMaxWidth()
                .focusRequester(focusRequester),
        )

        if (textState.text.isNotEmpty() && !isValid) {
            Text(
                text = when {
                    trimmed.length >= NOTE_CHARACTER_LIMIT -> {
                        stringResource(R.string.validation_text_note_character_limit, NOTE_CHARACTER_LIMIT)
                    }
                    else -> {
                        stringResource(R.string.validation_text_note)
                    }
                },
                color = Red400,
                style = TraktTheme.typography.meta,
                maxLines = 1,
                overflow = Ellipsis,
            )
        }

        if (error != null) {
            Text(
                text = stringResource(R.string.error_text_unexpected_notes_error),
                color = Red500,
                style = TraktTheme.typography.paragraphSmaller,
                maxLines = 3,
                overflow = Ellipsis,
                modifier = Modifier
                    .padding(top = 8.dp)
                    .onClick(onClick = onErrorClick),
            )
        }

        PrimaryButton(
            text = when {
                editor.note != null -> stringResource(R.string.button_text_edit_note)
                else -> stringResource(R.string.button_text_add_note)
            },
            enabled = !loading && isValid && isChanged,
            loading = loading,
            onClick = { onSubmitClick(trimmed) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp),
        )
    }
}
