package tv.trakt.trakt.core.search.data.local.people

import tv.trakt.trakt.common.helpers.extensions.nowUtcInstant
import tv.trakt.trakt.common.model.Person
import java.time.Instant

internal interface SearchPeopleLocalDataSource {
    suspend fun setPeople(
        people: List<Person>,
        createdAt: Instant = nowUtcInstant(),
    )

    suspend fun getPeople(): List<Person>
}
