package tv.trakt.trakt.core.summary.movies.features.actors.usecases

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import tv.trakt.trakt.common.model.CastPerson
import tv.trakt.trakt.common.model.Person
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.model.fromDto
import tv.trakt.trakt.core.movies.data.remote.MoviesRemoteDataSource

internal class GetMovieActorsUseCase(
    private val remoteSource: MoviesRemoteDataSource,
//    private val peopleLocalSource: PeopleLocalDataSource,
) {
    suspend fun getCastCrew(movieId: TraktId): ImmutableList<CastPerson> {
        val castCrew = remoteSource.getCastCrew(movieId)

        val cast = (castCrew.cast ?: emptyList())
            .take(30)
            .map { person ->
                CastPerson(
                    characters = person.characters,
                    person = Person.fromDto(person.person),
                )
            }

//        peopleLocalSource.upsertPeople(cast.map { it.person })

        return cast.toImmutableList()
    }
}
