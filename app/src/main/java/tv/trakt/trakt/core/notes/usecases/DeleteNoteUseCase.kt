package tv.trakt.trakt.core.notes.usecases

import tv.trakt.trakt.common.data.remote.NotesRemoteSource
import tv.trakt.trakt.common.model.UserNote

internal class DeleteNoteUseCase(
    private val remoteSource: NotesRemoteSource,
) {
    suspend fun deleteNote(note: UserNote) {
        remoteSource.deleteNote(
            noteId = note.id,
            type = note.type,
        )
    }
}
