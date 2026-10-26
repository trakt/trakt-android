package tv.trakt.trakt.common.networking.helpers

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

private val FatalOAuthErrorCodes = setOf(
    "invalid_grant",
    "invalid_client",
    "unauthorized_client",
    "invalid_scope",
    "access_denied",
    "login_required",
    "consent_required",
    "interaction_required",
)

/**
 * Whether a rejected token refresh means the session is gone for good. Only terminal OAuth
 * error codes count. Rate limits, timeouts and other failures keep the refresh token, so the
 * next request can try again.
 */
fun isFatalRefreshError(responseBody: String): Boolean {
    val code = runCatching {
        Json.parseToJsonElement(responseBody)
            .jsonObject["error"]
            ?.jsonPrimitive
            ?.contentOrNull
    }.getOrNull()

    return code in FatalOAuthErrorCodes
}
