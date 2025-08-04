package tv.trakt.trakt.app.core.people.data.local

import tv.trakt.trakt.app.common.model.Person
import tv.trakt.trakt.common.model.TraktId

internal interface PeopleLocalDataSource {
    suspend fun getPerson(personId: TraktId): Person?

    suspend fun upsertPeople(people: List<Person>)
}
