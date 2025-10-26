package tv.trakt.trakt.core.reactions.data

import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import tv.trakt.trakt.common.helpers.extensions.nowUtcInstant
import tv.trakt.trakt.core.reactions.data.ReactionsUpdates.Source
import java.time.Instant

internal class ReactionsUpdatesStorage : ReactionsUpdates {
    private val updatesMaps = Source.entries.associateWith {
        MutableSharedFlow<Pair<Int, Instant>>(
            replay = 1,
            onBufferOverflow = BufferOverflow.DROP_OLDEST,
        )
    }

    override fun notifyUpdate(
        commentId: Int,
        source: Source,
    ) {
        updatesMaps[source]?.tryEmit(
            Pair(commentId, nowUtcInstant()),
        )
    }

    override fun observeUpdates(source: Source): Flow<Pair<Int, Instant>> {
        return updatesMaps[source]?.asSharedFlow()!!
    }
}
