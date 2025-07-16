package tv.trakt.app.tv.core.details.movie.usecases

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import tv.trakt.app.tv.common.model.CastPerson
import tv.trakt.app.tv.common.model.Person
import tv.trakt.app.tv.common.model.TraktId
import tv.trakt.app.tv.common.model.fromDto
import tv.trakt.app.tv.core.movies.data.remote.MoviesRemoteDataSource
import tv.trakt.app.tv.core.people.data.local.PeopleLocalDataSource

internal class GetCastCrewUseCase(
    private val remoteSource: MoviesRemoteDataSource,
    private val peopleLocalSource: PeopleLocalDataSource,
) {
    suspend fun getCastCrew(movieId: TraktId): ImmutableList<CastPerson> {
        val castCrew = remoteSource.getMovieCastCrew(movieId)

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
