package tv.trakt.trakt.core.notes.usecases

import tv.trakt.trakt.common.data.remote.NotesRemoteSource
import tv.trakt.trakt.common.model.UserNote

internal class EditNoteUseCase(
    private val remoteSource: NotesRemoteSource,
) {
    suspend fun editNote(
        note: UserNote,
        text: String,
    ) {
        remoteSource.editNote(
            noteId = note.id,
            type = note.type,
            text = text,
        )
    }
}
