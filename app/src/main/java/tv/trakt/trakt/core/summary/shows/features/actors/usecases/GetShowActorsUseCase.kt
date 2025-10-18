package tv.trakt.trakt.core.summary.shows.features.actors.usecases

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import tv.trakt.trakt.common.model.CastPerson
import tv.trakt.trakt.common.model.Person
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.model.fromDto
import tv.trakt.trakt.core.people.data.local.PeopleLocalDataSource
import tv.trakt.trakt.core.shows.data.remote.ShowsRemoteDataSource

internal class GetShowActorsUseCase(
    private val remoteSource: ShowsRemoteDataSource,
    private val peopleLocalSource: PeopleLocalDataSource,
) {
    suspend fun getCastCrew(showId: TraktId): ImmutableList<CastPerson> {
        val castCrew = remoteSource.getCastCrew(showId)

        val cast = (castCrew.cast ?: emptyList())
            .distinctBy { it.person.ids.trakt }
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
