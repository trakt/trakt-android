package tv.trakt.trakt.core.lists.sections.watchlist.features.all.data

import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import tv.trakt.trakt.common.helpers.extensions.nowUtcInstant
import tv.trakt.trakt.core.lists.sections.watchlist.features.all.data.AllWatchlistLocalDataSource.Source
import java.time.Instant

internal class AllWatchlistStorage : AllWatchlistLocalDataSource {
    private val updatesMaps = Source.entries.associateWith {
        MutableSharedFlow<Instant?>(
            replay = 1,
            onBufferOverflow = BufferOverflow.DROP_OLDEST,
        )
    }

    override fun notifyUpdate(source: Source) {
        updatesMaps[source]?.tryEmit(nowUtcInstant())
    }

    override fun observeUpdates(source: Source): Flow<Instant?> {
        return updatesMaps[source]?.asSharedFlow()!!
    }
}
