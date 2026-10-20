package tv.trakt.trakt.core.summary.shows.features.actors.usecases

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import tv.trakt.trakt.common.data.local.PeopleLocalSource
import tv.trakt.trakt.common.data.remote.ShowsRemoteSource
import tv.trakt.trakt.common.model.Person
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.model.fromDto

private const val MAX_CREATORS = 2

internal class GetShowCreatorUseCase(
    private val remoteSource: ShowsRemoteSource,
    private val peopleLocalSource: PeopleLocalSource,
) {
    suspend fun getCreators(showId: TraktId): ImmutableList<Person> {
        val creators = remoteSource.getCastCrew(showId).crew
            ?.get("created by")
            .orEmpty()
            .take(MAX_CREATORS)
            .map { Person.fromDto(it.person) }

        peopleLocalSource.upsertPeople(creators)
        return creators.toImmutableList()
    }
}
