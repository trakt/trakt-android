package tv.trakt.trakt.core.discover.data.local.media

import tv.trakt.trakt.core.discover.model.DiscoverItem
import tv.trakt.trakt.core.discover.model.DiscoverSection

internal interface DiscoverMediaLocalDataSource {
    suspend fun setItems(
        section: DiscoverSection,
        items: List<DiscoverItem>,
    )

    suspend fun getItems(section: DiscoverSection): List<DiscoverItem>
}
