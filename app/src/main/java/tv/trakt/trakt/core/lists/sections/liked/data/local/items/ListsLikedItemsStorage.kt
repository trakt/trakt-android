package tv.trakt.trakt.core.lists.sections.liked.data.local.items

import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.core.lists.model.CustomListItem
import java.time.Instant

internal class ListsLikedItemsStorage : ListsLikedItemsLocalDataSource {
    private val mutex = Mutex()

    private val storage = mutableMapOf<TraktId, List<CustomListItem>>()
    private val updatedAt = MutableSharedFlow<Instant?>(
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
    )

    override suspend fun setItems(
        listId: TraktId,
        items: List<CustomListItem>,
    ) {
        mutex.withLock {
            storage[listId] = items
        }
    }

    override suspend fun getItems(listId: TraktId): List<CustomListItem> {
        return mutex.withLock {
            storage[listId] ?: emptyList()
        }
    }

    override fun clear() {
        storage.clear()
        updatedAt.tryEmit(null)
    }
}
