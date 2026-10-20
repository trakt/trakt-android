package tv.trakt.trakt.app.core.details.show.usecases

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import tv.trakt.trakt.common.data.local.PeopleLocalSource
import tv.trakt.trakt.common.data.remote.ShowsRemoteSource
import tv.trakt.trakt.common.model.CastPerson
import tv.trakt.trakt.common.model.Person
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.model.fromDto

internal class GetCastCrewUseCase(
    private val remoteSource: ShowsRemoteSource,
    private val peopleLocalSource: PeopleLocalSource,
) {
    suspend fun getCastCrew(showId: TraktId): ImmutableList<CastPerson> {
        val castCrew = remoteSource.getCastCrew(
            showId = showId,
            extended = "images",
        )

        val cast = (castCrew.cast ?: emptyList())
            .take(30)
            .map { person ->
                CastPerson(
                    characters = person.characters,
                    person = Person.fromDto(person.person),
                )
            }

        peopleLocalSource.upsertPeople(cast.map { it.person })

        return cast.toImmutableList()
    }
}
