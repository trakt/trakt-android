package tv.trakt.trakt.core.summary.movies.features.info.usecase

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import tv.trakt.trakt.common.helpers.extensions.EmptyImmutableList
import tv.trakt.trakt.common.model.CrewPerson
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.model.fromDto
import tv.trakt.trakt.core.movies.data.remote.MoviesRemoteDataSource
import tv.trakt.trakt.core.people.data.local.PeopleLocalDataSource

internal class GetMovieCrewUseCase(
    private val remoteSource: MoviesRemoteDataSource,
    private val peopleLocalSource: PeopleLocalDataSource,
) {
    suspend fun getCrew(movieId: TraktId): Result {
        return remoteSource.getCastCrew(movieId).crew?.let { crew ->
            val directors = crew["directing"]
                ?.filter { it.job.equals("director", ignoreCase = true) }
                ?.map { CrewPerson.fromDto(it) }
                ?: EmptyImmutableList

            val writers = crew["writing"]
                ?.map { CrewPerson.fromDto(it) }
                ?: EmptyImmutableList

            peopleLocalSource.upsertPeople((directors + writers).map { it.person })

            Result(
                directors = directors.toImmutableList(),
                writers = writers.toImmutableList(),
            )
        } ?: Result()
    }

    @Immutable
    data class Result(
        val directors: ImmutableList<CrewPerson> = EmptyImmutableList,
        val writers: ImmutableList<CrewPerson> = EmptyImmutableList,
    )
}
