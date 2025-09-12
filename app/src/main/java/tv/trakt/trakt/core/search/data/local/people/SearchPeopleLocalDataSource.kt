package tv.trakt.trakt.core.search.data.local.people

import tv.trakt.trakt.common.model.Person
import java.time.Instant

internal interface SearchPeopleLocalDataSource {
    suspend fun addPeople(
        movies: List<Person>,
        addedAt: Instant = Instant.now(),
    )

    suspend fun getPeople(): List<Person>
}
