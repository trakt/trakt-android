package tv.trakt.trakt.core.search.usecase

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import tv.trakt.trakt.common.helpers.extensions.asyncMap
import tv.trakt.trakt.common.helpers.extensions.nowLocalDay
import tv.trakt.trakt.common.model.Person
import tv.trakt.trakt.common.model.fromDto
import tv.trakt.trakt.core.people.data.remote.PeopleRemoteDataSource
import tv.trakt.trakt.core.search.data.local.people.SearchPeopleLocalDataSource

internal class GetBirthdayPeopleUseCase(
    private val remoteSource: PeopleRemoteDataSource,
    private val localSource: SearchPeopleLocalDataSource,
) {
    suspend fun getLocalPeople(): ImmutableList<Person> {
        val today = nowLocalDay()
        return localSource.getPeople()
            .sortedWith(
                compareByDescending<Person> {
                    it.birthday?.monthValue == today.monthValue &&
                        it.birthday?.dayOfMonth == today.dayOfMonth
                }.thenBy { it.birthday?.dayOfYear },
            )
            .toImmutableList()
    }

    suspend fun getPeople(limit: Int = 36): ImmutableList<Person> {
        val today = nowLocalDay()
        return remoteSource.getBirthdayPeople()
            .asyncMap {
                Person.fromDto(it)
            }
            .take(limit)
            .sortedWith(
                compareByDescending<Person> {
                    it.birthday?.monthValue == today.monthValue &&
                        it.birthday?.dayOfMonth == today.dayOfMonth
                }.thenBy { it.birthday?.dayOfYear },
            )
            .toImmutableList()
            .also { people ->
                localSource.setPeople(people = people)
            }
    }
}
