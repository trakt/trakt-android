package tv.trakt.trakt.common.core.lists.usecases

import android.content.res.Resources
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatDelegate
import io.ktor.client.plugins.ResponseException
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import tv.trakt.trakt.common.helpers.DynamicStringResource
import tv.trakt.trakt.common.helpers.StringResource
import tv.trakt.trakt.common.helpers.extensions.HTTP_ERROR_TRAKT_VIP_LIMIT
import tv.trakt.trakt.common.helpers.extensions.rethrowCancellation
import tv.trakt.trakt.resources.R
import java.text.NumberFormat
import java.util.Locale

private const val HEADER_ACCOUNT_LIMIT = "X-Account-Limit"
private const val HEADER_VIP_USER = "X-VIP-User"

class ListItemLimitException(
    val kind: Kind,
    val limit: Int,
    val isVip: Boolean,
    cause: Throwable,
) : Exception("List item limit reached: $kind", cause) {
    enum class Kind {
        SingleList,
        AllLists,
        Favorites,
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

        suspend fun fromFavoritesResponse(error: ResponseException): ListItemLimitException? {
            val limitError = fromResponse(error) ?: return null
            if (limitError.kind != Kind.SingleList) return limitError

            return ListItemLimitException(
                kind = Kind.Favorites,
                limit = limitError.limit,
                isVip = limitError.isVip,
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

fun ListItemLimitException.toUserMessage(resources: Resources): String {
    return resources.getString(messageResId(), formattedLimit())
}

fun ListItemLimitException.toStringResource(): StringResource {
    return DynamicStringResource(
        resId = messageResId(),
        args = listOf(formattedLimit()),
    )
}

private fun ListItemLimitException.formattedLimit(): String {
    val locale = AppCompatDelegate.getApplicationLocales().get(0) ?: Locale.getDefault()
    return NumberFormat.getNumberInstance(locale).format(limit)
}

@StringRes
private fun ListItemLimitException.messageResId(): Int {
    return when (kind) {
        ListItemLimitException.Kind.SingleList -> R.string.text_list_item_limit_reached
        ListItemLimitException.Kind.AllLists -> when {
            isVip -> R.string.text_lists_item_limit_reached_vip
            else -> R.string.text_lists_item_limit_reached_free
        }
        ListItemLimitException.Kind.Favorites -> when {
            isVip -> R.string.text_favorites_item_limit_reached_vip
            else -> R.string.text_favorites_item_limit_reached_free
        }
    }
}
