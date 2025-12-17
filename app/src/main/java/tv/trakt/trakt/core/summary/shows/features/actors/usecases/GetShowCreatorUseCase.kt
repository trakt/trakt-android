package tv.trakt.trakt.core.summary.shows.features.actors.usecases

import tv.trakt.trakt.common.model.Person
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.model.fromDto
import tv.trakt.trakt.core.people.data.local.PeopleLocalDataSource
import tv.trakt.trakt.core.shows.data.remote.ShowsRemoteDataSource

internal class GetShowCreatorUseCase(
    private val remoteSource: ShowsRemoteDataSource,
    private val peopleLocalSource: PeopleLocalDataSource,
) {
    suspend fun getCreator(showId: TraktId): Person {
        return remoteSource.getCastCrew(showId).crew
            ?.get("created by")
            ?.firstOrNull()
            ?.let { Person.fromDto(it.person) }
            ?.also {
                peopleLocalSource.upsertPeople(listOf(it))
            } ?: Person.Unknown
    }
}
