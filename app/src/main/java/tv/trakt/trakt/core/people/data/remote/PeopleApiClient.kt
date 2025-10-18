package tv.trakt.trakt.core.people.data.remote

import org.openapitools.client.apis.PeopleApi
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.networking.PersonDto
import tv.trakt.trakt.common.networking.PersonMoviesDto
import tv.trakt.trakt.common.networking.PersonShowsDto
import tv.trakt.trakt.core.people.data.remote.api.PeopleExtrasApi

internal class PeopleApiClient(
    private val peopleExtrasApi: PeopleExtrasApi,
    private val peopleApi: PeopleApi,
) : PeopleRemoteDataSource {
    override suspend fun getPersonDetails(personId: TraktId): PersonDto {
        val response = peopleApi.getPeopleSummary(
            id = personId.value.toString(),
            extended = "full,cloud9",
        )
        return response.body()
    }

    override suspend fun getPersonShowsCredits(personId: TraktId): PersonShowsDto {
        val response = peopleApi.getPeopleShows(
            id = personId.value.toString(),
            extended = "full,cloud9,colors,streaming_ids",
        )
        return response.body()
    }

    override suspend fun getPersonMoviesCredits(personId: TraktId): PersonMoviesDto {
        val response = peopleApi.getPeopleMovies(
            id = personId.value.toString(),
            extended = "full,cloud9,colors,streaming_ids",
        )
        return response.body()
    }

    override suspend fun getBirthdayPeople(): List<PersonDto> {
        val response = peopleExtrasApi.getBirthdayPeople(
            extended = "full,cloud9",
        )
        return response.body()
    }
}
