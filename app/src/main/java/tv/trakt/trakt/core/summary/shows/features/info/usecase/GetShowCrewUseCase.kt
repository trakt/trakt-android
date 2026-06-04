package tv.trakt.trakt.core.summary.shows.features.info.usecase

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import tv.trakt.trakt.common.helpers.extensions.EmptyImmutableList
import tv.trakt.trakt.common.model.Person
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
                ?.take(5)
                ?.map { Person.fromDto(it.person) }
                ?: EmptyImmutableList

            val writers = crew["writing"]
                ?.take(5)
                ?.map { Person.fromDto(it.person) }
                ?: EmptyImmutableList

            peopleLocalSource.upsertPeople(creators + writers)

            Result(
                creators = creators.toImmutableList(),
                writers = writers.toImmutableList(),
            )
        } ?: Result()
    }

    @Immutable
    data class Result(
        val creators: ImmutableList<Person> = EmptyImmutableList,
        val writers: ImmutableList<Person> = EmptyImmutableList,
    )
}
