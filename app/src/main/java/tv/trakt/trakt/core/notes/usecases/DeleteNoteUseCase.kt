package tv.trakt.trakt.core.notes.usecases

import tv.trakt.trakt.common.model.UserNote
import tv.trakt.trakt.common.networking.api.v3.V3Api
import tv.trakt.trakt.common.networking.api.v3.model.V3DeleteNoteRequest

internal class DeleteNoteUseCase(
    private val v3Api: V3Api,
) {
    suspend fun deleteNote(note: UserNote) {
        v3Api.deleteNote(
            noteId = note.id,
            request = V3DeleteNoteRequest(type = note.type.value),
        )
    }
}
