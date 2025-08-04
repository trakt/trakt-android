package tv.trakt.trakt.app.core.people.usecases

import tv.trakt.trakt.app.common.model.Person
import tv.trakt.trakt.app.common.model.fromDto
import tv.trakt.trakt.app.core.people.data.local.PeopleLocalDataSource
import tv.trakt.trakt.app.core.people.data.remote.PeopleRemoteDataSource
import tv.trakt.trakt.common.model.TraktId

internal class GetPersonUseCase(
    private val peopleLocalSource: PeopleLocalDataSource,
    private val peopleRemoteSource: PeopleRemoteDataSource,
) {
    suspend fun getPerson(personId: TraktId): Person? {
        val person = peopleLocalSource.getPerson(personId)
        if (person == null) {
            val remotePerson = peopleRemoteSource.getPersonDetails(personId)

            val person = Person.fromDto(remotePerson)
            peopleLocalSource.upsertPeople(listOf(person))

            return person
        }
        return person
    }

    suspend fun getPersonDetails(personId: TraktId): Person {
        val response = peopleRemoteSource.getPersonDetails(personId)
        val person = Person.fromDto(response)

        peopleLocalSource.upsertPeople(listOf(person))

        return person
    }
}
