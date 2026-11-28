package tv.trakt.trakt.common.data.remote

import tv.trakt.trakt.common.model.MediaType
import tv.trakt.trakt.common.model.NoteType
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.networking.api.v3.V3NotesApi
import tv.trakt.trakt.common.networking.api.v3.model.V3DeleteNoteRequest
import tv.trakt.trakt.common.networking.api.v3.model.V3EditNoteRequest
import tv.trakt.trakt.common.networking.api.v3.model.V3PostNoteRequest
import tv.trakt.trakt.common.networking.api.v3.model.V3UserNoteResponse

class NotesRemoteSource(
    private val v3NotesApi: V3NotesApi,
) {
    suspend fun getNotes(
        mediaType: MediaType,
        mediaId: TraktId,
    ): List<V3UserNoteResponse> {
        return v3NotesApi.getUserNotes(mediaType, mediaId)
    }

    suspend fun postNote(
        mediaType: MediaType,
        mediaId: TraktId,
        type: NoteType,
        text: String,
    ) {
        v3NotesApi.postNote(
            V3PostNoteRequest(
                media = V3PostNoteRequest.Media(
                    type = mediaType.value,
                    id = mediaId.value,
                ),
                type = type.value,
                notes = text,
            ),
        )
    }

    suspend fun editNote(
        noteId: Int,
        type: NoteType,
        text: String,
    ) {
        v3NotesApi.putNote(
            noteId = noteId,
            request = V3EditNoteRequest(
                type = type.value,
                notes = text,
            ),
        )
    }

    suspend fun deleteNote(
        noteId: Int,
        type: NoteType,
    ) {
        v3NotesApi.deleteNote(
            noteId = noteId,
            request = V3DeleteNoteRequest(type = type.value),
        )
    }
}
