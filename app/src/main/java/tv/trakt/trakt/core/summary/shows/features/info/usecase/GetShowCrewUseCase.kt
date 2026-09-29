package tv.trakt.trakt.core.summary.shows.features.info.usecase

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import tv.trakt.trakt.common.helpers.extensions.EmptyImmutableList
import tv.trakt.trakt.common.model.CrewPerson
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.model.fromDto
import tv.trakt.trakt.core.people.data.local.PeopleLocalDataSource
import tv.trakt.trakt.core.shows.data.remote.ShowsRemoteDataSource

internal class GetShowCrewUseCase(
    private val remoteSource: ShowsRemoteDataSource,
    private val peopleLocalSource: PeopleLocalDataSource,
) {
    suspend fun getCrew(showId: TraktId): Result {
        return remoteSource.getCastCrew(showId).crew?.let { crew ->
            val creators = crew["created by"]
                ?.filter { member -> member.jobs.any { it.equals("creator", ignoreCase = true) } }
                ?.map { CrewPerson.fromDto(it) }
                ?: EmptyImmutableList

            val writers = crew["writing"]
                ?.map { CrewPerson.fromDto(it) }
                ?: EmptyImmutableList

            peopleLocalSource.upsertPeople((creators + writers).map { it.person })

            Result(
                creators = creators.toImmutableList(),
                writers = writers.toImmutableList(),
            )
        } ?: Result()
    }

    @Immutable
    data class Result(
        val creators: ImmutableList<CrewPerson> = EmptyImmutableList,
        val writers: ImmutableList<CrewPerson> = EmptyImmutableList,
    )
}
