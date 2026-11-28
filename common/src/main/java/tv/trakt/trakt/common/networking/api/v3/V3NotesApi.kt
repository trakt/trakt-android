package tv.trakt.trakt.common.networking.api.v3

import io.ktor.client.HttpClientConfig
import io.ktor.client.call.body
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import org.openapitools.client.infrastructure.ApiClient
import tv.trakt.trakt.common.model.MediaType
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.networking.api.v3.model.V3DeleteNoteRequest
import tv.trakt.trakt.common.networking.api.v3.model.V3EditNoteRequest
import tv.trakt.trakt.common.networking.api.v3.model.V3PostNoteRequest
import tv.trakt.trakt.common.networking.api.v3.model.V3UserNoteResponse

class V3NotesApi(
    private val baseV3Url: String,
    httpClientEngine: HttpClientEngine,
    httpClientConfig: ((HttpClientConfig<*>) -> Unit),
) : ApiClient(
        baseV3Url,
        httpClientEngine,
        httpClientConfig,
    ) {
    suspend fun getUserNotes(
        type: MediaType,
        mediaId: TraktId,
    ): List<V3UserNoteResponse> {
        val response = client.get("${baseV3Url}users/me/notes/${type.value}/${mediaId.value}")
        return response.body()
    }

    suspend fun postNote(request: V3PostNoteRequest) {
        client.post("${baseV3Url}users/me/notes") {
            setBody(request)
        }
    }

    suspend fun putNote(
        noteId: Int,
        request: V3EditNoteRequest,
    ) {
        client.put("${baseV3Url}users/me/notes/$noteId") {
            setBody(request)
        }
    }

    suspend fun deleteNote(
        noteId: Int,
        request: V3DeleteNoteRequest,
    ) {
        client.delete("${baseV3Url}users/me/notes/$noteId") {
            setBody(request)
        }
    }
}
