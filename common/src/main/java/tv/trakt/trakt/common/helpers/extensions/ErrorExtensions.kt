package tv.trakt.trakt.common.helpers.extensions

import kotlin.coroutines.cancellation.CancellationException

fun Exception.rethrowCancellation(action: () -> Unit = {}) {
    if (this is CancellationException) {
        throw this
    } else {
        action()
    }
}
