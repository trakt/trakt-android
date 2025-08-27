package tv.trakt.trakt.core.home.sections.activity.data.local.social

import tv.trakt.trakt.core.home.sections.activity.model.HomeActivityItem

internal interface HomeSocialLocalDataSource {
    suspend fun addItems(items: List<HomeActivityItem>)

    suspend fun getItems(): List<HomeActivityItem>
}
