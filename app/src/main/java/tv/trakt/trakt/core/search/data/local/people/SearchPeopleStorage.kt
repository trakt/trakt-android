package tv.trakt.trakt.core.search.data.local.people

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import tv.trakt.trakt.common.model.Person
import tv.trakt.trakt.common.model.TraktId
import java.time.Instant

internal class SearchPeopleStorage : SearchPeopleLocalDataSource {
    private val mutex = Mutex()
    private val moviesCache = mutableMapOf<TraktId, Person>()

    override suspend fun addPeople(
        movies: List<Person>,
        addedAt: Instant,
    ) {
        mutex.withLock {
            with(moviesCache) {
                clear()
                putAll(movies.associateBy { it.ids.trakt })
            }
        }
    }

    override suspend fun getPeople(): List<Person> {
        return mutex.withLock {
            moviesCache.values.toList()
        }
    }
}
