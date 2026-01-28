package tv.trakt.trakt.common.helpers.extensions

import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.plugins.ResponseException
import kotlin.coroutines.cancellation.CancellationException

const val HTTP_ERROR_TRAKT_VIP_ONLY = 426
const val HTTP_ERROR_TRAKT_LISTS_LIMIT = 420

fun Exception.rethrowCancellation(action: () -> Unit = {}) {
    if (this is CancellationException) {
        throw this
    } else {
        action()
    }
}

fun Exception.getHttpErrorCode(): Int? {
    return when (this) {
        is ClientRequestException -> response.status.value
        is ResponseException -> response.status.value
        else -> null
    }
}
