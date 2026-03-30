package tv.trakt.trakt.core.lists.sections.collaborations.data.local.lists

import kotlinx.coroutines.flow.Flow
import tv.trakt.trakt.common.model.CustomList
import tv.trakt.trakt.common.model.TraktId
import java.time.Instant
import java.time.ZonedDateTime

internal interface ListsCollaborationsLocalDataSource {
    suspend fun setLists(items: List<CustomList>)

    suspend fun getLists(): List<CustomList>

    suspend fun onUpdatedAt(
        id: TraktId,
        updatedAt: ZonedDateTime,
    )

    suspend fun notifyUpdate()

    fun observeUpdates(): Flow<Instant?>

    fun clear()
}
