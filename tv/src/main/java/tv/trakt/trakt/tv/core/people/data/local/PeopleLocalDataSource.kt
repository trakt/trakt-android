package tv.trakt.trakt.tv.core.people.data.local

import tv.trakt.trakt.tv.common.model.Person
import tv.trakt.trakt.tv.common.model.TraktId

internal interface PeopleLocalDataSource {
    suspend fun getPerson(personId: TraktId): Person?

    suspend fun upsertPeople(people: List<Person>)
}
