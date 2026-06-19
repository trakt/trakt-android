package tv.trakt.trakt.core.lists.features.reorder.data

import kotlinx.coroutines.flow.Flow
import tv.trakt.trakt.common.model.TraktId
import java.time.Instant

internal interface ReorderUpdates {
    fun notifyUpdate(listId: TraktId)

    fun observeUpdates(listId: TraktId): Flow<Instant>

    fun clear()
}
