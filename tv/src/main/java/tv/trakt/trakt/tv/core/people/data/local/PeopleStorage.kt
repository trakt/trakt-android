package tv.trakt.trakt.tv.core.people.data.local

import kotlinx.collections.immutable.persistentMapOf
import tv.trakt.trakt.tv.common.model.Person
import tv.trakt.trakt.tv.common.model.TraktId
import java.util.concurrent.ConcurrentHashMap

// TODO Temporary cache implementation, replace with a proper database solution later.
internal class PeopleStorage : PeopleLocalDataSource {
    private val cache = ConcurrentHashMap<TraktId, Person>(persistentMapOf())

    override suspend fun getPerson(personId: TraktId): Person? {
        return cache[personId]
    }

    override suspend fun upsertPeople(people: List<Person>) {
        val map = people.associateBy { it.ids.trakt }
        cache.putAll(map)
    }
}
