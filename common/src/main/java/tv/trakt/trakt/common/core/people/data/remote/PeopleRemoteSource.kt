package tv.trakt.trakt.common.core.people.data.remote

import org.openapitools.client.apis.PeopleApi
import tv.trakt.trakt.common.helpers.extensions.rethrowCancellation
import tv.trakt.trakt.common.model.SlugId
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.networking.PersonDto
import tv.trakt.trakt.common.networking.PersonMoviesDto
import tv.trakt.trakt.common.networking.PersonShowsDto
import tv.trakt.trakt.common.networking.api.people.PeopleExtrasApi

class PeopleRemoteSource(
    private val peopleExtrasApi: PeopleExtrasApi,
    private val peopleApi: PeopleApi,
) {
    suspend fun getPersonDetails(
        personId: TraktId,
        extended: String = "full,images,markdown",
    ): PersonDto {
        return getPersonSummary(personId.value.toString(), extended)
    }

    suspend fun getPersonDetails(
        personSlug: SlugId,
        extended: String = "full,images,markdown",
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

    suspend fun getPersonShowsCredits(
        personId: TraktId,
        extended: String = "full,images,colors,streaming_ids",
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

    suspend fun getPersonMoviesCredits(
        personId: TraktId,
        extended: String = "full,images,colors,streaming_ids",
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

    suspend fun getBirthdayPeople(): List<PersonDto> {
        val response = peopleExtrasApi.getBirthdayPeople(
            extended = "full,images,markdown",
        )
        return response.body()
    }
}
