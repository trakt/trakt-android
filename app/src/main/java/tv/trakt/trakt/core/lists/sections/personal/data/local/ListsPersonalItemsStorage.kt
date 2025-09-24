package tv.trakt.trakt.core.lists.sections.personal.data.local

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.core.lists.model.PersonalListItem

internal class ListsPersonalItemsStorage : ListsPersonalItemsLocalDataSource {
    private val mutex = Mutex()
    private val storage = mutableMapOf<TraktId, List<PersonalListItem>>()

    override suspend fun addItems(
        listId: TraktId,
        items: List<PersonalListItem>,
    ) {
        mutex.withLock {
            storage[listId] = items
        }
    }

    override suspend fun getItems(listId: TraktId): List<PersonalListItem> {
        return mutex.withLock {
            storage[listId] ?: emptyList()
        }
    }

    override suspend fun removeShows(
        listId: TraktId,
        showsIds: List<TraktId>,
    ) {
        mutex.withLock {
            val currentItems = storage[listId] ?: return
            storage[listId] = currentItems
                .filterIsInstance<PersonalListItem.ShowItem>()
                .filterNot { it.show.ids.trakt in showsIds }
        }
    }

    override suspend fun removeMovies(
        listId: TraktId,
        moviesIds: List<TraktId>,
    ) {
        mutex.withLock {
            val currentItems = storage[listId] ?: return
            storage[listId] = currentItems
                .filterIsInstance<PersonalListItem.MovieItem>()
                .filterNot { it.movie.ids.trakt in moviesIds }
        }
    }

    override fun clear() {
        storage.clear()
    }
}
