package tv.trakt.trakt.core.people.usecases

import androidx.appcompat.app.AppCompatDelegate
import tv.trakt.trakt.common.data.local.PeopleLocalSource
import tv.trakt.trakt.common.data.remote.PeopleRemoteSource
import tv.trakt.trakt.common.helpers.extensions.apiLanguage
import tv.trakt.trakt.common.model.Person
import tv.trakt.trakt.common.model.SlugId
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.model.fromDto
import java.util.Locale

internal class GetPersonUseCase(
    private val peopleLocalSource: PeopleLocalSource,
    private val peopleRemoteSource: PeopleRemoteSource,
) {
    suspend fun getPerson(personId: TraktId): Person {
        val person = peopleLocalSource.getPerson(personId)

        if (person == null) {
            val remotePerson = peopleRemoteSource.getPersonDetails(
                personId = personId,
                language = appLanguage(),
            )
            val person = Person.fromDto(remotePerson)
            return person.also {
                peopleLocalSource.upsertPeople(listOf(it))
            }
        }

        return person
    }

    suspend fun getPersonDetails(personId: TraktId): Person {
        val response = peopleRemoteSource.getPersonDetails(
            personId = personId,
            language = appLanguage(),
        )
        val person = Person.fromDto(response)

        peopleLocalSource.upsertPeople(listOf(person))

        return person
    }

    suspend fun getPersonDetails(personSlug: SlugId): Person {
        val response = peopleRemoteSource.getPersonDetails(
            personSlug = personSlug,
            language = appLanguage(),
        )
        val person = Person.fromDto(response)

        peopleLocalSource.upsertPeople(listOf(person))

        return person
    }

    // The API answers a malformed language with 400, so only two-letter codes are sent.
    private fun appLanguage(): String? {
        val locale = AppCompatDelegate.getApplicationLocales().get(0) ?: Locale.getDefault()
        return locale.apiLanguage.takeIf { it.length == 2 }
    }
}
