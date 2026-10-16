package tv.trakt.trakt.common.core.people.data.remote

import tv.trakt.trakt.common.model.SlugId
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.networking.PersonDto
import tv.trakt.trakt.common.networking.PersonMoviesDto
import tv.trakt.trakt.common.networking.PersonShowsDto

interface PeopleRemoteDataSource {
    suspend fun getPersonDetails(
        personId: TraktId,
        extended: String = "full,images,markdown",
    ): PersonDto

    suspend fun getPersonDetails(
        personSlug: SlugId,
        extended: String = "full,images,markdown",
    ): PersonDto

    suspend fun getPersonShowsCredits(
        personId: TraktId,
        extended: String = "full,images,colors,streaming_ids",
    ): PersonShowsDto

    suspend fun getPersonMoviesCredits(
        personId: TraktId,
        extended: String = "full,images,colors,streaming_ids",
    ): PersonMoviesDto

    suspend fun getBirthdayPeople(): List<PersonDto>
}
