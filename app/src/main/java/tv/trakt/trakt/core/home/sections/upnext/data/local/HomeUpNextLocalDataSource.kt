package tv.trakt.trakt.core.home.sections.upnext.data.local

import kotlinx.coroutines.flow.Flow
import tv.trakt.trakt.common.core.home.model.UpNextItem
import tv.trakt.trakt.common.model.TraktId
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
