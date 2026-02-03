package tv.trakt.trakt.common.helpers.lifecycle

import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.filter
import tv.trakt.trakt.common.helpers.lifecycle.AppLifecycleProvider.State

class DefaultAppLifecycleProvider : AppLifecycleProvider {
    private val stateFlow = MutableSharedFlow<State>(
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
    )

    override fun notify(source: State) {
        stateFlow.tryEmit(source)
    }

    override fun observeState(state: State): Flow<State> {
        return stateFlow
            .filter { it == state }
    }
}
