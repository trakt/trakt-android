package tv.trakt.trakt.core.home.sections.upnext.features.all.data.local

import kotlinx.coroutines.flow.Flow
import java.time.Instant

internal interface AllUpNextLocalDataSource {
    fun notifyUpdate()

    fun observeUpdates(): Flow<Instant?>
}
