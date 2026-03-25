package tv.trakt.trakt.core.user.data.local.watchlist

import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import tv.trakt.trakt.common.helpers.extensions.nowUtcInstant
import tv.trakt.trakt.core.user.data.local.watchlist.WatchlistUpdates.Source
import java.time.Instant

internal class WatchlistUpdatesStorage : WatchlistUpdates {
    private val updatesMaps = Source.entries.associateWith {
        MutableSharedFlow<Instant>(
            extraBufferCapacity = 1,
            onBufferOverflow = BufferOverflow.DROP_OLDEST,
        )
    }

    override fun notifyUpdate(source: Source) {
        updatesMaps[source]
            ?.tryEmit(nowUtcInstant())
    }

    override fun observeUpdates(source: Source): Flow<Instant> {
        return updatesMaps[source] ?: throw IllegalArgumentException("Unknown source: $source")
    }
}
