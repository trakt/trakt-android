package tv.trakt.trakt.core.lists.features.reorder.data

import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import tv.trakt.trakt.common.helpers.extensions.nowUtcInstant
import tv.trakt.trakt.common.model.TraktId
import java.time.Instant
import java.util.concurrent.ConcurrentHashMap

internal class ReorderUpdatesStorage : ReorderUpdates {
    private val updatesMaps = ConcurrentHashMap<TraktId, MutableSharedFlow<Instant>>()

    override fun notifyUpdate(listId: TraktId) {
        updatesMaps[listId]
            ?.tryEmit(nowUtcInstant())
    }

    override fun observeUpdates(listId: TraktId): Flow<Instant> =
        updatesMaps.computeIfAbsent(listId) {
            MutableSharedFlow(
                extraBufferCapacity = 1,
                onBufferOverflow = BufferOverflow.DROP_OLDEST,
            )
        }

    override fun clear() {
        updatesMaps.clear()
    }
}
