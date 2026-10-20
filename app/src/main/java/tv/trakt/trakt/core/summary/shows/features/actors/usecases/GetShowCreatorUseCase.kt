package tv.trakt.trakt.core.summary.shows.features.actors.usecases

import tv.trakt.trakt.common.data.local.PeopleLocalSource
import tv.trakt.trakt.common.data.remote.ShowsRemoteSource
import tv.trakt.trakt.common.model.Person
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.model.fromDto

internal class GetShowCreatorUseCase(
    private val remoteSource: ShowsRemoteSource,
    private val peopleLocalSource: PeopleLocalSource,
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
