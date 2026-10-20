package tv.trakt.trakt.app.core.people.usecases

import tv.trakt.trakt.common.data.local.PeopleLocalSource
import tv.trakt.trakt.common.data.remote.PeopleRemoteSource
import tv.trakt.trakt.common.model.Person
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.model.fromDto

private const val PERSON_EXTENDED = "full,images"

internal class GetPersonUseCase(
    private val peopleLocalSource: PeopleLocalSource,
    private val peopleRemoteSource: PeopleRemoteSource,
) {
    suspend fun getPerson(personId: TraktId): Person {
        val person = peopleLocalSource.getPerson(personId)
        if (person == null) {
            val remotePerson = peopleRemoteSource.getPersonDetails(
                personId = personId,
                extended = PERSON_EXTENDED,
            )

            val person = Person.fromDto(remotePerson)
            peopleLocalSource.upsertPeople(listOf(person))

            return person
        }
        return person
    }

    suspend fun getPersonDetails(personId: TraktId): Person {
        val response = peopleRemoteSource.getPersonDetails(
            personId = personId,
            extended = PERSON_EXTENDED,
        )
        val person = Person.fromDto(response)

        peopleLocalSource.upsertPeople(listOf(person))

        return person
    }
}
