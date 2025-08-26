package tv.trakt.trakt.core.home.sections.upnext.data.local

import tv.trakt.trakt.core.home.sections.upnext.model.ProgressShow

internal interface HomeUpNextLocalDataSource {
    suspend fun addItems(items: List<ProgressShow>)

    suspend fun getItems(): List<ProgressShow>
}
