package tv.trakt.trakt.core.home.sections.upnext.data.local

import tv.trakt.trakt.core.home.sections.upnext.model.ProgressShow
import java.time.Instant

internal interface HomeUpNextLocalDataSource {
    suspend fun addItems(
        shows: List<ProgressShow>,
        addedAt: Instant = Instant.now(),
    )

    suspend fun getItems(): List<ProgressShow>
}
