package tv.trakt.trakt.core.lists.sections.liked.data.local.lists

import kotlinx.coroutines.flow.Flow
import tv.trakt.trakt.common.model.CustomList
import tv.trakt.trakt.common.model.TraktId
import java.time.Instant
import java.time.ZonedDateTime

internal interface ListsLikedLocalDataSource {
    suspend fun setItems(items: List<CustomList>)

    suspend fun getItems(): List<CustomList>

    suspend fun onUpdatedAt(
        id: TraktId,
        updatedAt: ZonedDateTime,
    )

    fun observeUpdates(): Flow<Instant?>

    fun clear()
}
