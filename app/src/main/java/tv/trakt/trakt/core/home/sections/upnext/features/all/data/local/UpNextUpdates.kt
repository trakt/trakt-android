package tv.trakt.trakt.core.home.sections.upnext.features.all.data.local

import kotlinx.coroutines.flow.Flow
import java.time.Instant

internal interface UpNextUpdates {
    fun notifyUpdate(source: Source)

    fun observeUpdates(vararg sources: Source): Flow<Instant?>

    enum class Source {
        Default,
        Home,
        Widget,
    }
}
