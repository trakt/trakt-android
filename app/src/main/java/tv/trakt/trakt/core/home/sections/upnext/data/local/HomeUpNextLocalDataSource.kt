package tv.trakt.trakt.core.home.sections.upnext.data.local

import kotlinx.coroutines.flow.Flow
import tv.trakt.trakt.core.home.sections.upnext.model.ProgressShow
import java.time.Instant

internal interface HomeUpNextLocalDataSource {
    suspend fun addItems(items: List<ProgressShow>)

    suspend fun getItems(): List<ProgressShow>

    fun observeUpdated(): Flow<Instant?>

    fun clear()
}
