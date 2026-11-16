package tv.trakt.trakt.core.notes.usecases

import tv.trakt.trakt.common.data.remote.NotesRemoteSource
import tv.trakt.trakt.common.model.MediaType
import tv.trakt.trakt.common.model.NoteType
import tv.trakt.trakt.common.model.TraktId

internal class PostNoteUseCase(
    private val remoteSource: NotesRemoteSource,
) {
    suspend fun postNote(
        mediaType: MediaType,
        mediaId: TraktId,
        type: NoteType,
        text: String,
    ) {
        remoteSource.postNote(
            mediaType = mediaType,
            mediaId = mediaId,
            type = type,
            text = text,
        )
    }
}
