package tv.trakt.trakt.app.core.people.data.remote

import org.openapitools.client.apis.PeopleApi
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.networking.PersonDto
import tv.trakt.trakt.common.networking.PersonMoviesDto
import tv.trakt.trakt.common.networking.PersonShowsDto

internal class PeopleApiClient(
    private val api: PeopleApi,
) : PeopleRemoteDataSource {
    override suspend fun getPersonDetails(personId: TraktId): PersonDto {
        val response = api.getPeopleSummary(
            id = personId.value.toString(),
            extended = "full,cloud9",
        )
        return response.body()
    }

    override suspend fun getPersonShowsCredits(personId: TraktId): PersonShowsDto {
        val response = api.getPeopleShows(
            id = personId.value.toString(),
            extended = "full,cloud9,colors,streaming_ids",
        )
        return response.body()
    }

    override suspend fun getPersonMoviesCredits(personId: TraktId): PersonMoviesDto {
        val response = api.getPeopleMovies(
            id = personId.value.toString(),
            extended = "full,cloud9,colors,streaming_ids",
        )
        return response.body()
    }
}
