package tv.trakt.trakt.core.people.data.remote

import tv.trakt.trakt.common.networking.PersonDto
import tv.trakt.trakt.core.people.data.remote.api.PeopleExtrasApi

internal class PeopleApiClient(
    private val api: PeopleExtrasApi,
) : PeopleRemoteDataSource {
    override suspend fun getBirthdayPeople(): List<PersonDto> {
        val response = api.getBirthdayPeople(
            extended = "full,cloud9",
        )
        return response.body()
    }
}
