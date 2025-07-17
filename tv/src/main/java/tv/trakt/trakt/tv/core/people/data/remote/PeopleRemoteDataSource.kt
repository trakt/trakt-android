package tv.trakt.trakt.tv.core.people.data.remote

import tv.trakt.trakt.tv.common.model.TraktId
import tv.trakt.trakt.tv.networking.openapi.PersonDetailsDto
import tv.trakt.trakt.tv.networking.openapi.PersonMoviesDto
import tv.trakt.trakt.tv.networking.openapi.PersonShowsDto

internal interface PeopleRemoteDataSource {
    suspend fun getPersonDetails(personId: TraktId): PersonDetailsDto

    suspend fun getPersonShowsCredits(personId: TraktId): PersonShowsDto

    suspend fun getPersonMoviesCredits(personId: TraktId): PersonMoviesDto
}
