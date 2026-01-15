package tv.trakt.trakt.core.people.usecases

import tv.trakt.trakt.common.model.Person
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.model.fromDto
import tv.trakt.trakt.core.people.data.local.PeopleLocalDataSource
import tv.trakt.trakt.core.people.data.remote.PeopleRemoteDataSource

internal class GetPersonUseCase(
    private val peopleLocalSource: PeopleLocalDataSource,
    private val peopleRemoteSource: PeopleRemoteDataSource,
) {
    suspend fun getPerson(personId: TraktId): Person {
        val person = peopleLocalSource.getPerson(personId)

        if (person == null) {
            val remotePerson = peopleRemoteSource.getPersonDetails(personId)
            val person = Person.fromDto(remotePerson)
            return person.also {
                peopleLocalSource.upsertPeople(listOf(it))
            }
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
