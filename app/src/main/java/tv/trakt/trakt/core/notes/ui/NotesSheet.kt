@file:OptIn(ExperimentalMaterial3Api::class)

package tv.trakt.trakt.core.notes.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.SheetState
import androidx.compose.material3.SheetValue.Expanded
import androidx.compose.material3.SheetValue.Hidden
import androidx.compose.material3.Text
import androidx.compose.material3.rememberBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight.Companion.W600
import androidx.compose.ui.text.style.TextOverflow.Companion.Ellipsis
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.launch
import tv.trakt.trakt.common.helpers.extensions.relativeDateString
import tv.trakt.trakt.common.model.NoteType
import tv.trakt.trakt.common.model.UserNote
import tv.trakt.trakt.core.comments.ui.CommentDropdown
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.components.TraktBottomSheet
import tv.trakt.trakt.ui.components.TraktHeader
import tv.trakt.trakt.ui.components.confirmation.RemoveConfirmationSheet
import tv.trakt.trakt.ui.theme.DefaultCardShape
import tv.trakt.trakt.ui.theme.TraktTheme
import java.time.ZonedDateTime

@Composable
internal fun NotesSheet(
    state: SheetState = rememberBottomSheetState(
        initialValue = Hidden,
        enabledValues = setOf(Hidden, Expanded),
    ),
    active: Boolean,
    title: String,
    notes: ImmutableList<UserNote>?,
    deleting: Boolean,
    onEditClick: (UserNote) -> Unit,
    onDeleteConfirm: (UserNote) -> Unit,
    onDismiss: () -> Unit,
) {
    val sheetScope = rememberCoroutineScope()
    var deleteTarget by remember { mutableStateOf<UserNote?>(null) }

    if (active) {
        TraktBottomSheet(
            sheetState = state,
            onDismiss = onDismiss,
        ) {
            NotesView(
                title = title,
                notes = notes ?: persistentListOf(),
                deleting = deleting,
                onEditClick = { note ->
                    sheetScope.launch { state.hide() }
                        .invokeOnCompletion {
                            if (!state.isVisible) {
                                onDismiss()
                                onEditClick(note)
                            }
                        }
                },
                onDeleteClick = { deleteTarget = it },
                modifier = Modifier
                    .padding(horizontal = 24.dp)
                    .padding(bottom = 24.dp),
            )
        }
    }

    RemoveConfirmationSheet(
        active = deleteTarget != null,
        title = stringResource(R.string.confirmation_title_delete_note),
        message = stringResource(R.string.warning_prompt_delete_note),
        onYes = {
            deleteTarget?.let(onDeleteConfirm)
            deleteTarget = null
        },
        onNo = { deleteTarget = null },
    )
}

@Composable
private fun NotesView(
    title: String,
    notes: ImmutableList<UserNote>,
    deleting: Boolean,
    onEditClick: (UserNote) -> Unit,
    onDeleteClick: (UserNote) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        verticalArrangement = spacedBy(12.dp),
        modifier = modifier
            .alpha(if (deleting) 0.5F else 1F)
            .verticalScroll(
                state = rememberScrollState(),
                overscrollEffect = null,
            ),
    ) {
        TraktHeader(
            title = stringResource(R.string.drawer_title_notes),
            subtitle = title,
            modifier = Modifier.padding(bottom = 4.dp),
        )

        if (notes.isEmpty()) {
            NotesPlaceholder()
        }

        notes.forEach { note ->
            NoteCard(
                note = note,
                enabled = !deleting,
                onEditClick = { onEditClick(note) },
                onDeleteClick = { onDeleteClick(note) },
            )
        }
    }
}

@Composable
private fun NotesPlaceholder(modifier: Modifier = Modifier) {
    Column(
        verticalArrangement = spacedBy(6.dp),
        modifier = modifier
            .fillMaxWidth()
            .background(TraktTheme.colors.dialogOnContainer, DefaultCardShape)
            .padding(16.dp),
    ) {
        Text(
            text = stringResource(R.string.header_notes_placeholder),
            color = TraktTheme.colors.textPrimary,
            style = TraktTheme.typography.paragraph.copy(fontWeight = W600),
        )
        Text(
            text = stringResource(R.string.notes_placeholder_text),
            color = TraktTheme.colors.textSecondary,
            style = TraktTheme.typography.paragraphSmaller,
        )
    }
}

@Composable
private fun NoteCard(
    note: UserNote,
    enabled: Boolean,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        verticalArrangement = spacedBy(12.dp),
        modifier = modifier
            .fillMaxWidth()
            .background(TraktTheme.colors.dialogOnContainer, DefaultCardShape)
            .padding(16.dp),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = spacedBy(10.dp),
        ) {
            Icon(
                painter = painterResource(
                    when (note.type) {
                        NoteType.Favorites -> R.drawable.ic_heart_on
                        NoteType.Note -> R.drawable.ic_notes
                    },
                ),
                contentDescription = null,
                tint = TraktTheme.colors.textPrimary,
                modifier = Modifier
                    .border(1.dp, TraktTheme.colors.separator, CircleShape)
                    .size(24.dp),
            )

            Column(modifier = Modifier.weight(1F)) {
                Text(
                    text = stringResource(
                        when (note.type) {
                            NoteType.Favorites -> R.string.list_title_favorites
                            NoteType.Note -> R.string.text_personal_note
                        },
                    ),
                    color = TraktTheme.colors.textPrimary,
                    style = TraktTheme.typography.cardTitle.copy(
                        fontSize = 14.sp,
                    ),
                    maxLines = 1,
                    overflow = Ellipsis,
                )
                Text(
                    text = note.updatedAt.toLocalDate().relativeDateString(),
                    color = TraktTheme.colors.textSecondary,
                    style = TraktTheme.typography.cardSubtitle,
                    maxLines = 1,
                    overflow = Ellipsis,
                )
            }

            if (enabled) {
                CommentDropdown(
                    onEditClick = onEditClick,
                    onDeleteClick = onDeleteClick,
                )
            }
        }

        Text(
            text = note.text,
            color = TraktTheme.colors.textPrimary,
            style = TraktTheme.typography.paragraphSmall,
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF212427)
@Composable
private fun Preview() {
    TraktTheme {
        NotesView(
            title = "The Matrix",
            notes = persistentListOf(
                UserNote(
                    id = 1,
                    text = "Rewatched it again and it still holds up.",
                    type = NoteType.Favorites,
                    createdAt = ZonedDateTime.now(),
                    updatedAt = ZonedDateTime.now(),
                ),
            ),
            deleting = false,
            onEditClick = {},
            onDeleteClick = {},
            modifier = Modifier.padding(24.dp),
        )
    }
}
