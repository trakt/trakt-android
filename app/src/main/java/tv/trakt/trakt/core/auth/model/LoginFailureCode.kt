package tv.trakt.trakt.core.auth.model

import tv.trakt.trakt.common.helpers.extensions.getHttpCode
import java.io.IOException

internal fun Exception.toLoginFailureCode(): String {
    val httpCode = getHttpCode()
    return when {
        httpCode != null -> "http_$httpCode"
        this is IOException -> "network"
        else -> "unknown"
    }
}
