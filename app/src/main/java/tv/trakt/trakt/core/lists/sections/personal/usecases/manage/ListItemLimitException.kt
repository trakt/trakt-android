package tv.trakt.trakt.core.lists.sections.personal.usecases.manage

import io.ktor.client.plugins.ResponseException
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import tv.trakt.trakt.common.helpers.extensions.HTTP_ERROR_TRAKT_VIP_LIMIT
import tv.trakt.trakt.common.helpers.extensions.rethrowCancellation

private const val HEADER_ACCOUNT_LIMIT = "X-Account-Limit"
private const val HEADER_VIP_USER = "X-VIP-User"

internal class ListItemLimitException(
    val kind: Kind,
    val limit: Int,
    val isVip: Boolean,
    cause: Throwable,
) : Exception("List item limit reached: $kind", cause) {
    enum class Kind {
        SingleList,
        AllLists,
    }

    companion object {
        suspend fun fromResponse(error: ResponseException): ListItemLimitException? {
            val response = error.response
            if (response.status.value != HTTP_ERROR_TRAKT_VIP_LIMIT) return null

            val kind = when (errorCode(response.readBody())) {
                "list_item_limit_reached" -> Kind.SingleList
                "total_list_item_limit_reached" -> Kind.AllLists
                else -> return null
            }

            val limit = response.headers[HEADER_ACCOUNT_LIMIT]
                ?.toIntOrNull()
                ?.takeIf { it > 0 }
                ?: return null

            return ListItemLimitException(
                kind = kind,
                limit = limit,
                isVip = response.headers[HEADER_VIP_USER] == "true",
                cause = error,
            )
        }

        private suspend fun HttpResponse.readBody(): String? {
            return try {
                bodyAsText()
            } catch (error: Exception) {
                error.rethrowCancellation()
                null
            }
        }

        private fun errorCode(body: String?): String? {
            if (body == null) return null
            return runCatching {
                Json.parseToJsonElement(body)
                    .jsonObject["error"]
                    ?.jsonPrimitive
                    ?.contentOrNull
            }.getOrNull()
        }
    }
}
