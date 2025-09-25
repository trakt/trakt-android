package tv.trakt.trakt.core.home.sections.activity.all.data.local

import kotlinx.coroutines.flow.Flow
import java.time.Instant

internal interface AllActivityLocalDataSource {
    fun notifyUpdate()

    fun observeUpdates(): Flow<Instant?>
}
