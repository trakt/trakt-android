package tv.trakt.trakt.core.lists.sections.liked.data.local.lists

import kotlinx.coroutines.flow.Flow
import tv.trakt.trakt.common.model.CustomList
import java.time.Instant

internal interface ListsLikedLocalDataSource {
    suspend fun setItems(items: List<CustomList>)

    suspend fun getItems(): List<CustomList>

    suspend fun notifyUpdate()

    fun observeUpdates(): Flow<Instant?>

    fun clear()
}
