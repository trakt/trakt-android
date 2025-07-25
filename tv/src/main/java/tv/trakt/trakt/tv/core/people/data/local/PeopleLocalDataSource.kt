package tv.trakt.trakt.tv.core.people.data.local

import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.tv.common.model.Person

internal interface PeopleLocalDataSource {
    suspend fun getPerson(personId: TraktId): Person?

    suspend fun upsertPeople(people: List<Person>)
}
