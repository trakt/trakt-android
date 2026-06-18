package tv.trakt.trakt.core.lists.sections.personal.data.local

import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import tv.trakt.trakt.common.helpers.extensions.nowUtcInstant
import tv.trakt.trakt.common.model.Movie
import tv.trakt.trakt.common.model.Show
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.core.lists.model.CustomListItem
import java.time.Instant

internal class ListsPersonalItemsStorage : ListsPersonalItemsLocalDataSource {
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

    override suspend fun addShows(
        listId: TraktId,
        shows: List<Show>,
        notify: Boolean,
    ) {
        mutex.withLock {
            val currentItems = storage[listId] ?: emptyList()
            val newItems = shows.mapIndexed { _, show ->
                CustomListItem.ShowItem(
                    show = show,
                    itemId = 0,
                    rank = Int.MAX_VALUE,
                    listedAt = nowUtcInstant(),
                )
            }

            storage[listId] = (currentItems + newItems)
                .distinctBy {
                    if (it is CustomListItem.ShowItem) {
                        it.key
                    } else {
                        null
                    }
                }
        }

        if (notify) {
            updatedAt.tryEmit(nowUtcInstant())
        }
    }

    override suspend fun removeShows(
        listId: TraktId,
        showsIds: List<TraktId>,
        notify: Boolean,
    ) {
        mutex.withLock {
            val currentItems = storage[listId] ?: return
            storage[listId] = currentItems
                .filterNot {
                    it is CustomListItem.ShowItem &&
                        it.show.ids.trakt in showsIds
                }
        }
        if (notify) {
            updatedAt.tryEmit(nowUtcInstant())
        }
    }

    override suspend fun addMovies(
        listId: TraktId,
        movies: List<Movie>,
        notify: Boolean,
    ) {
        mutex.withLock {
            val currentItems = storage[listId] ?: emptyList()
            val newItems = movies.mapIndexed { _, movie ->
                CustomListItem.MovieItem(
                    movie = movie,
                    itemId = 0,
                    rank = Int.MAX_VALUE,
                    listedAt = nowUtcInstant(),
                )
            }

            storage[listId] = (currentItems + newItems)
                .distinctBy {
                    if (it is CustomListItem.MovieItem) {
                        it.key
                    } else {
                        null
                    }
                }
        }

        if (notify) {
            updatedAt.tryEmit(nowUtcInstant())
        }
    }

    override suspend fun removeMovies(
        listId: TraktId,
        moviesIds: List<TraktId>,
        notify: Boolean,
    ) {
        mutex.withLock {
            val currentItems = storage[listId] ?: return
            storage[listId] = currentItems
                .filterNot {
                    it is CustomListItem.MovieItem &&
                        it.movie.ids.trakt in moviesIds
                }
        }
        if (notify) {
            updatedAt.tryEmit(nowUtcInstant())
        }
    }

    override fun observeUpdates(): Flow<Instant?> {
        return updatedAt.asSharedFlow()
    }

    override fun clear() {
        storage.clear()
        updatedAt.tryEmit(null)
    }
}
