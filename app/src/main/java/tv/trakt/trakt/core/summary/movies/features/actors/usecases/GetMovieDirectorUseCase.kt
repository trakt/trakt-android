package tv.trakt.trakt.core.summary.movies.features.actors.usecases

import tv.trakt.trakt.common.model.Person
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.model.fromDto
import tv.trakt.trakt.core.movies.data.remote.MoviesRemoteDataSource
import tv.trakt.trakt.core.people.data.local.PeopleLocalDataSource

internal class GetMovieDirectorUseCase(
    private val remoteSource: MoviesRemoteDataSource,
    private val peopleLocalSource: PeopleLocalDataSource,
) {
    suspend fun getDirector(movieId: TraktId): Person {
        return remoteSource.getCastCrew(movieId).crew
            ?.get("directing")
            ?.firstOrNull { it.job.equals("director", ignoreCase = true) }
            ?.let { Person.fromDto(it.person) }
            ?.also {
                peopleLocalSource.upsertPeople(listOf(it))
            } ?: Person.Unknown
    }
}
