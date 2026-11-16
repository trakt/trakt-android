package tv.trakt.trakt.core.notes

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import tv.trakt.trakt.common.helpers.LoadingState
import tv.trakt.trakt.common.model.NoteType
import tv.trakt.trakt.common.model.UserNote

internal const val NOTE_CHARACTER_LIMIT = 500

@Immutable
internal data class NotesState(
    val notes: ImmutableList<UserNote>? = null,
    val editor: NoteEditor? = null,
    val loadingEditor: LoadingState = LoadingState.Idle,
    val loadingDelete: LoadingState = LoadingState.Idle,
    val error: Exception? = null,
) {
    val hasNotes: Boolean
        get() = notes?.isNotEmpty() == true

    @Immutable
    data class NoteEditor(
        val type: NoteType,
        val note: UserNote? = null,
    )
}
