package tv.trakt.trakt.core.lists.features.details.info.usecases

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import tv.trakt.trakt.common.data.remote.ListsRemoteSource
import tv.trakt.trakt.common.model.Person
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.model.fromDto

internal class GetListFeaturedPeopleUseCase(
    private val remoteSource: ListsRemoteSource,
) {
    suspend fun getPeople(listId: TraktId): ImmutableList<Person> {
        return remoteSource.getListFeaturedPeople(listId)
            .map { Person.fromDto(it) }
            .toImmutableList()
    }
}
