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
import tv.trakt.trakt.core.lists.model.PersonalListItem
import java.time.Instant

internal class ListsPersonalItemsStorage : ListsPersonalItemsLocalDataSource {
    private val mutex = Mutex()

    private val storage = mutableMapOf<TraktId, List<PersonalListItem>>()
    private val updatedAt = MutableSharedFlow<Instant?>(
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
    )

    override suspend fun setItems(
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

    override suspend fun addShows(
        listId: TraktId,
        shows: List<Show>,
        notify: Boolean,
    ) {
        mutex.withLock {
            val currentItems = storage[listId] ?: emptyList()
            val newItems = shows.mapIndexed { index, show ->
                PersonalListItem.ShowItem(
                    show = show,
                    rank = currentItems.size + (index + 1),
                    listedAt = nowUtcInstant(),
                )
            }

            storage[listId] = (newItems + currentItems)
                .distinctBy {
                    if (it is PersonalListItem.ShowItem) {
                        it.show.ids.trakt
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
                    it is PersonalListItem.ShowItem &&
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
            val newItems = movies.mapIndexed { index, movie ->
                PersonalListItem.MovieItem(
                    movie = movie,
                    rank = currentItems.size + (index + 1),
                    listedAt = nowUtcInstant(),
                )
            }

            storage[listId] = (newItems + currentItems)
                .distinctBy {
                    if (it is PersonalListItem.MovieItem) {
                        it.movie.ids.trakt
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
                    it is PersonalListItem.MovieItem &&
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
