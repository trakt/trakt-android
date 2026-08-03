package tv.trakt.trakt.core.lists.sections.smart.data.local

import kotlinx.coroutines.flow.Flow
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.model.lists.SmartList
import java.time.Instant

internal interface ListsSmartLocalDataSource {
    suspend fun setLists(items: List<SmartList>)

    suspend fun getLists(): List<SmartList>

    suspend fun removeList(listId: TraktId)

    suspend fun notifyUpdate()

    fun observeUpdates(): Flow<Instant?>

    suspend fun clear()
}
