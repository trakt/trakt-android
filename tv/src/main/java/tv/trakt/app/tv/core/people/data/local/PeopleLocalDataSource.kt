package tv.trakt.app.tv.core.people.data.local

import tv.trakt.app.tv.common.model.Person
import tv.trakt.app.tv.common.model.TraktId

internal interface PeopleLocalDataSource {
    suspend fun getPerson(personId: TraktId): Person?

    suspend fun upsertPeople(people: List<Person>)
}
