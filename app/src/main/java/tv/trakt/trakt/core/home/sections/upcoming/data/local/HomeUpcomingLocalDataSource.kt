package tv.trakt.trakt.core.home.sections.upcoming.data.local

import kotlinx.coroutines.flow.Flow
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.core.home.sections.upcoming.model.HomeUpcomingItem
import java.time.Instant

internal interface HomeUpcomingLocalDataSource {
    suspend fun addItems(items: List<HomeUpcomingItem>)

    suspend fun getItems(): List<HomeUpcomingItem>

    suspend fun removeShowItems(
        showIds: List<TraktId>,
        notify: Boolean,
    )

    fun observeUpdates(): Flow<Instant?>

    fun clear()
}
