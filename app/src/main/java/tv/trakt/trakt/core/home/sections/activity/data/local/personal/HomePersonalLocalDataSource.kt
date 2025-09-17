package tv.trakt.trakt.core.home.sections.activity.data.local.personal

import tv.trakt.trakt.core.home.sections.activity.model.HomeActivityItem

internal interface HomePersonalLocalDataSource {
    suspend fun addItems(items: List<HomeActivityItem>)

    suspend fun getItems(): List<HomeActivityItem>

    suspend fun removeItems(ids: Set<Long>)

    fun clear()
}
