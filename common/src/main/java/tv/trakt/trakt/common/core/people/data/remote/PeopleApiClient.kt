package tv.trakt.trakt.common.core.people.data.remote

import org.openapitools.client.apis.PeopleApi
import tv.trakt.trakt.common.helpers.extensions.rethrowCancellation
import tv.trakt.trakt.common.model.SlugId
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.networking.PersonDto
import tv.trakt.trakt.common.networking.PersonMoviesDto
import tv.trakt.trakt.common.networking.PersonShowsDto
import tv.trakt.trakt.common.networking.api.people.PeopleExtrasApi

internal class PeopleApiClient(
    private val peopleExtrasApi: PeopleExtrasApi,
    private val peopleApi: PeopleApi,
) : PeopleRemoteDataSource {
    override suspend fun getPersonDetails(
        personId: TraktId,
        extended: String,
    ): PersonDto {
        return getPersonSummary(personId.value.toString(), extended)
    }

    override suspend fun getPersonDetails(
        personSlug: SlugId,
        extended: String,
    ): PersonDto {
        return getPersonSummary(personSlug.value, extended)
    }

    private suspend fun getPersonSummary(
        id: String,
        extended: String,
    ): PersonDto {
        val response = peopleApi.getPeopleSummary(
            id = id,
            extended = extended,
        )
        return response.body()
    }

    override suspend fun getPersonShowsCredits(
        personId: TraktId,
        extended: String,
    ): PersonShowsDto {
        return try {
            peopleApi.getPeopleShows(
                id = personId.value.toString(),
                extended = extended,
            ).body()
        } catch (error: Exception) {
            // People without show credits return 204 No Content, which fails body parsing.
            error.rethrowCancellation()
            PersonShowsDto()
        }
    }

    override suspend fun getPersonMoviesCredits(
        personId: TraktId,
        extended: String,
    ): PersonMoviesDto {
        return try {
            peopleApi.getPeopleMovies(
                id = personId.value.toString(),
                extended = extended,
            ).body()
        } catch (error: Exception) {
            // People without movie credits return 204 No Content, which fails body parsing.
            error.rethrowCancellation()
            PersonMoviesDto()
        }
    }

    override suspend fun getBirthdayPeople(): List<PersonDto> {
        val response = peopleExtrasApi.getBirthdayPeople(
            extended = "full,images,markdown",
        )
        return response.body()
    }
}
