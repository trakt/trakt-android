package tv.trakt.trakt.tv.core.people.data.remote

import org.openapitools.client.apis.PeopleApi
import tv.trakt.trakt.common.networking.PersonDetailsDto
import tv.trakt.trakt.common.networking.PersonMoviesDto
import tv.trakt.trakt.common.networking.PersonShowsDto
import tv.trakt.trakt.tv.common.model.TraktId

internal class PeopleApiClient(
    private val api: PeopleApi,
) : PeopleRemoteDataSource {
    override suspend fun getPersonDetails(personId: TraktId): PersonDetailsDto {
        val response = api.getPeopleSummary(
            id = personId.value.toString(),
            extended = "full,cloud9",
        )
        return response.body()
    }

    override suspend fun getPersonShowsCredits(personId: TraktId): PersonShowsDto {
        val response = api.getPeopleShows(
            id = personId.value.toString(),
            extended = "full,cloud9,colors",
        )
        return response.body()
    }

    override suspend fun getPersonMoviesCredits(personId: TraktId): PersonMoviesDto {
        val response = api.getPeopleMovies(
            id = personId.value.toString(),
            extended = "full,cloud9,colors",
        )
        return response.body()
    }
}
