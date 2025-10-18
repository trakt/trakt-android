package tv.trakt.trakt.core.people.data.local

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import tv.trakt.trakt.common.model.Person
import tv.trakt.trakt.common.model.TraktId

internal class PeopleStorage : PeopleLocalDataSource {
    private val mutex = Mutex()
    private val storage = mutableMapOf<TraktId, Person>()

    override suspend fun getPerson(personId: TraktId): Person? {
        return mutex.withLock {
            storage[personId]
        }
    }

    override suspend fun upsertPeople(people: List<Person>) {
        mutex.withLock {
            with(storage) {
                putAll(people.associateBy { it.ids.trakt })
            }
        }
    }
}
