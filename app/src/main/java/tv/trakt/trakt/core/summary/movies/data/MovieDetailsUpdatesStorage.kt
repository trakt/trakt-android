package tv.trakt.trakt.core.summary.movies.data

import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import tv.trakt.trakt.common.helpers.extensions.nowUtcInstant
import tv.trakt.trakt.core.summary.movies.data.MovieDetailsUpdates.Source
import java.time.Instant

internal class MovieDetailsUpdatesStorage : MovieDetailsUpdates {
    private val updatesMaps = Source.entries.associateWith {
        MutableSharedFlow<Instant?>(
            extraBufferCapacity = 1,
            onBufferOverflow = BufferOverflow.DROP_OLDEST,
        )
    }

    override fun notifyUpdate(source: Source) {
        updatesMaps[source]?.tryEmit(nowUtcInstant())
    }

    override fun observeUpdates(source: Source): Flow<Instant?> {
        return updatesMaps.getValue(source).asSharedFlow()
    }
}
