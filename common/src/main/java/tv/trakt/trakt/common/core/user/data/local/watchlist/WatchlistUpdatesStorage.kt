package tv.trakt.trakt.common.core.user.data.local.watchlist

import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import tv.trakt.trakt.common.helpers.extensions.nowUtcInstant
import java.time.Instant

class WatchlistUpdatesStorage : WatchlistUpdates {
    private val updatesMaps = WatchlistUpdates.Source.entries.associateWith {
        MutableSharedFlow<Instant>(
            extraBufferCapacity = 1,
            onBufferOverflow = BufferOverflow.DROP_OLDEST,
        )
    }

    override fun notifyUpdate(source: WatchlistUpdates.Source) {
        updatesMaps[source]
            ?.tryEmit(nowUtcInstant())
    }

    override fun observeUpdates(source: WatchlistUpdates.Source): Flow<Instant> {
        return updatesMaps[source] ?: throw IllegalArgumentException("Unknown source: $source")
    }
}
