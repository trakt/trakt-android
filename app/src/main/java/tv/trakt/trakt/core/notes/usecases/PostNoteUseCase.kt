package tv.trakt.trakt.core.notes.usecases

import tv.trakt.trakt.common.model.MediaType
import tv.trakt.trakt.common.model.NoteType
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.networking.api.v3.V3Api
import tv.trakt.trakt.common.networking.api.v3.model.V3PostNoteRequest

internal class PostNoteUseCase(
    private val v3Api: V3Api,
) {
    suspend fun postNote(
        mediaType: MediaType,
        mediaId: TraktId,
        type: NoteType,
        text: String,
    ) {
        v3Api.postNote(
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
}
