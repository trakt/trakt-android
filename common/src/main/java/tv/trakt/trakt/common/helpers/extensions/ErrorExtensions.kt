package tv.trakt.trakt.common.helpers.extensions

import io.ktor.client.plugins.ClientRequestException
import kotlin.coroutines.cancellation.CancellationException

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
        else -> null
    }
}
