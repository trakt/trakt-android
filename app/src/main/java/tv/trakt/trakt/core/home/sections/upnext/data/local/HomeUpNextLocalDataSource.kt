package tv.trakt.trakt.core.home.sections.upnext.data.local

import kotlinx.coroutines.flow.Flow
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.core.home.sections.upnext.model.UpNextItem
import java.time.Instant

internal interface HomeUpNextLocalDataSource {
    suspend fun addItems(items: List<UpNextItem>)

    suspend fun setItems(items: List<UpNextItem>)

    suspend fun getItems(): List<UpNextItem>

    suspend fun removeShowItems(ids: List<TraktId>)

    suspend fun removeMovieItems(ids: List<TraktId>)

    fun notifyUpdate()

    fun observeUpdates(): Flow<Instant?>

    fun clear()
}
