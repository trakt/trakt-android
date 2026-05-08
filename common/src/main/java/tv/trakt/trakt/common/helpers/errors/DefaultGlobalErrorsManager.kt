package tv.trakt.trakt.common.helpers.errors

import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.filter
import kotlin.coroutines.cancellation.CancellationException

class DefaultGlobalErrorsManager : GlobalErrorsManager {
    private val flow = MutableSharedFlow<Exception?>(
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
    )

    override fun tryEmit(error: Exception) {
        flow.tryEmit(error)
    }

    override fun observe(): Flow<Exception?> {
        return flow
            .filter { it !is CancellationException }
    }

    override fun clear() {
        flow.tryEmit(null)
    }
}
