package tv.trakt.trakt.core.home.sections.upnext.data.local

import tv.trakt.trakt.common.core.home.model.UpNextItem
import tv.trakt.trakt.common.model.TraktId

internal interface HomeUpNextLocalDataSource {
    suspend fun addItems(items: List<UpNextItem>)

    suspend fun setItems(items: List<UpNextItem>)

    suspend fun getItems(): List<UpNextItem>

    suspend fun removeShowItems(ids: List<TraktId>)

    suspend fun removeMovieItems(ids: List<TraktId>)

    fun clear()
}
