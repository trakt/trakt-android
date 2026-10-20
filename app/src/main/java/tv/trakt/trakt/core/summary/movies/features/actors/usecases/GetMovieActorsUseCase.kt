package tv.trakt.trakt.core.summary.movies.features.actors.usecases

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import tv.trakt.trakt.common.data.local.PeopleLocalSource
import tv.trakt.trakt.common.data.remote.MoviesRemoteSource
import tv.trakt.trakt.common.model.CastPerson
import tv.trakt.trakt.common.model.Person
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.model.fromDto

internal class GetMovieActorsUseCase(
    private val remoteSource: MoviesRemoteSource,
    private val peopleLocalSource: PeopleLocalSource,
) {
    suspend fun getCastCrew(movieId: TraktId): ImmutableList<CastPerson> {
        val castCrew = remoteSource.getCastCrew(movieId)

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
