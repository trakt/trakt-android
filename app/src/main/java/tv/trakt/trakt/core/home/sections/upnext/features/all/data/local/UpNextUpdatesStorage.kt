package tv.trakt.trakt.core.home.sections.upnext.features.all.data.local

import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.merge
import tv.trakt.trakt.common.helpers.extensions.nowUtcInstant
import tv.trakt.trakt.core.home.sections.upnext.features.all.data.local.UpNextUpdates.Source
import java.time.Instant

internal class UpNextUpdatesStorage : UpNextUpdates {
    private val updatesMaps = Source.entries.associateWith {
        MutableSharedFlow<Instant?>(
            extraBufferCapacity = 1,
            onBufferOverflow = BufferOverflow.DROP_OLDEST,
        )
    }

    override fun notifyUpdate(source: Source) {
        updatesMaps[source]?.tryEmit(nowUtcInstant())
    }

    override fun observeUpdates(vararg sources: Source): Flow<Instant?> {
        return merge(
            *sources
                .map { updatesMaps[it] ?: emptyFlow() }
                .toTypedArray(),
        )
    }
}
