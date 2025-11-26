package tv.trakt.trakt.core.home.sections.upnext.features.all.data.local

import kotlinx.coroutines.flow.Flow
import java.time.Instant

internal interface UpNextUpdates {
    fun notifyUpdate()

    fun observeUpdates(): Flow<Instant?>
}
