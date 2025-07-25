package tv.trakt.trakt.tv.core.details.show.usecases

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.tv.common.model.CastPerson
import tv.trakt.trakt.tv.common.model.Person
import tv.trakt.trakt.tv.common.model.fromDto
import tv.trakt.trakt.tv.core.people.data.local.PeopleLocalDataSource
import tv.trakt.trakt.tv.core.shows.data.remote.ShowsRemoteDataSource

internal class GetCastCrewUseCase(
    private val remoteSource: ShowsRemoteDataSource,
    private val peopleLocalSource: PeopleLocalDataSource,
) {
    suspend fun getCastCrew(showId: TraktId): ImmutableList<CastPerson> {
        val castCrew = remoteSource.getShowCastCrew(showId)

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
