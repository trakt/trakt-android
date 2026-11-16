package tv.trakt.trakt.core.notes.usecases

import tv.trakt.trakt.common.model.UserNote
import tv.trakt.trakt.common.networking.api.v3.V3Api
import tv.trakt.trakt.common.networking.api.v3.model.V3EditNoteRequest

internal class EditNoteUseCase(
    private val v3Api: V3Api,
) {
    suspend fun editNote(
        note: UserNote,
        text: String,
    ) {
        v3Api.putNote(
            noteId = note.id,
            request = V3EditNoteRequest(
                type = note.type.value,
                notes = text,
            ),
        )
    }
}
