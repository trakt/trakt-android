package tv.trakt.trakt.common.data.remote

import org.openapitools.client.apis.PeopleApi
import org.openapitools.client.models.PostPeopleReportRequest
import tv.trakt.trakt.common.helpers.extensions.rethrowCancellation
import tv.trakt.trakt.common.model.SlugId
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.model.globalfilter.GlobalFilter
import tv.trakt.trakt.common.model.globalfilter.toApiParams
import tv.trakt.trakt.common.networking.PersonDto
import tv.trakt.trakt.common.networking.PersonMoviesDto
import tv.trakt.trakt.common.networking.PersonShowsDto
import tv.trakt.trakt.common.networking.api.people.PeopleExtrasApi

class PeopleRemoteSource(
    private val peopleExtrasApi: PeopleExtrasApi,
    private val peopleApi: PeopleApi,
    private val authorizedPeopleApi: PeopleApi,
) {
    suspend fun getPersonDetails(
        personId: TraktId,
        extended: String = "full,images,markdown",
        language: String? = null,
    ): PersonDto {
        return getPersonSummary(personId.value.toString(), extended, language)
    }

    suspend fun getPersonDetails(
        personSlug: SlugId,
        extended: String = "full,images,markdown",
        language: String? = null,
    ): PersonDto {
        return getPersonSummary(personSlug.value, extended, language)
    }

    private suspend fun getPersonSummary(
        id: String,
        extended: String,
        language: String?,
    ): PersonDto {
        val response = peopleApi.getPeopleSummary(
            id = id,
            extended = extended,
            language = language,
        )
        return response.body()
    }

    suspend fun getPersonShowsCredits(
        personId: TraktId,
        filters: GlobalFilter = GlobalFilter.Default,
        extended: String = "full,images,colors,streaming_ids",
    ): PersonShowsDto {
        return try {
            peopleExtrasApi.getPeopleShows(
                id = personId.value.toString(),
                extended = extended,
                filters = filters.toApiParams(),
            ).body()
        } catch (error: Exception) {
            // People without show credits return 204 No Content, which fails body parsing.
            error.rethrowCancellation()
            PersonShowsDto()
        }
    }

    suspend fun getPersonMoviesCredits(
        personId: TraktId,
        filters: GlobalFilter = GlobalFilter.Default,
        extended: String = "full,images,colors,streaming_ids",
    ): PersonMoviesDto {
        return try {
            peopleExtrasApi.getPeopleMovies(
                id = personId.value.toString(),
                extended = extended,
                filters = filters.toApiParams(),
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

    suspend fun postReport(
        personId: TraktId,
        reason: String,
        message: String,
    ) {
        authorizedPeopleApi.postPeopleReport(
            id = personId.value.toString(),
            postPeopleReportRequest = PostPeopleReportRequest(
                reason = PostPeopleReportRequest.Reason.entries.first { it.value == reason },
                message = message.ifBlank { null },
            ),
        )
    }
}
