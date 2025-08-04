package tv.trakt.trakt.app.core.details.show.usecases

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import tv.trakt.trakt.app.common.model.CastPerson
import tv.trakt.trakt.app.common.model.Person
import tv.trakt.trakt.app.common.model.fromDto
import tv.trakt.trakt.app.core.people.data.local.PeopleLocalDataSource
import tv.trakt.trakt.app.core.shows.data.remote.ShowsRemoteDataSource
import tv.trakt.trakt.common.model.TraktId

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
