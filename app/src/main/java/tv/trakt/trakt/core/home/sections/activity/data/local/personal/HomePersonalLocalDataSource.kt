package tv.trakt.trakt.core.home.sections.activity.data.local.personal

import kotlinx.coroutines.flow.Flow
import tv.trakt.trakt.core.home.sections.activity.model.HomeActivityItem
import java.time.Instant

internal interface HomePersonalLocalDataSource {
    suspend fun addItems(
        items: List<HomeActivityItem>,
        notify: Boolean,
    )

    suspend fun getItems(): List<HomeActivityItem>

    suspend fun removeItems(
        ids: Set<Long>,
        notify: Boolean,
    )

    fun observeUpdates(): Flow<Instant?>

    fun clear()
}
