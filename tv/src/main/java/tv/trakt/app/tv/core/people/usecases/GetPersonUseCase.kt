package tv.trakt.app.tv.core.people.usecases

import tv.trakt.app.tv.common.model.Person
import tv.trakt.app.tv.common.model.TraktId
import tv.trakt.app.tv.common.model.fromDto
import tv.trakt.app.tv.core.people.data.local.PeopleLocalDataSource
import tv.trakt.app.tv.core.people.data.remote.PeopleRemoteDataSource

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
