package tv.trakt.trakt.common.networking.api.v3

import io.ktor.client.HttpClientConfig
import io.ktor.client.call.body
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.put
import org.openapitools.client.infrastructure.ApiClient
import tv.trakt.trakt.common.model.MediaType
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.networking.api.v3.model.reactions.V3MediaReactionsSummaryResponse
import tv.trakt.trakt.common.networking.api.v3.model.reactions.V3UserMediaReaction

class V3ReactionsApi(
    private val baseV3Url: String,
    httpClientEngine: HttpClientEngine,
    httpClientConfig: ((HttpClientConfig<*>) -> Unit),
) : ApiClient(
        baseV3Url,
        httpClientEngine,
        httpClientConfig,
    ) {
    suspend fun getMediaReactionsSummary(
        type: MediaType,
        mediaId: TraktId,
    ): V3MediaReactionsSummaryResponse {
        val response = client.get("${mediaReactionsUrl(type, mediaId)}/summary")
        return response.body()
    }

    suspend fun getUserMediaReactions(
        type: MediaType,
        mediaId: TraktId,
    ): List<V3UserMediaReaction> {
        val response = client.get("${baseV3Url}users/me/${type.value}/${mediaId.value}")
        return response.body()
    }

    suspend fun putMediaReactions(
        type: MediaType,
        mediaId: TraktId,
        reactions: List<String>,
    ): List<V3UserMediaReaction> {
        val response = client.put("${mediaReactionsUrl(type, mediaId)}/${reactions.joinToString(",")}")
        return response.body()
    }

    suspend fun deleteMediaReactions(
        type: MediaType,
        mediaId: TraktId,
        reactionIds: List<Long>,
    ) {
        client.delete("${mediaReactionsUrl(type, mediaId)}/${reactionIds.joinToString(",")}")
    }

    private fun mediaReactionsUrl(
        type: MediaType,
        mediaId: TraktId,
    ): String {
        return "${baseV3Url}${type.value}s/${mediaId.value}/reactions"
    }
}
