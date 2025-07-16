package tv.trakt.app.tv.core.people.data.remote

import tv.trakt.app.tv.common.model.TraktId
import tv.trakt.app.tv.networking.openapi.PersonDetailsDto
import tv.trakt.app.tv.networking.openapi.PersonMoviesDto
import tv.trakt.app.tv.networking.openapi.PersonShowsDto

internal interface PeopleRemoteDataSource {
    suspend fun getPersonDetails(personId: TraktId): PersonDetailsDto

    suspend fun getPersonShowsCredits(personId: TraktId): PersonShowsDto

    suspend fun getPersonMoviesCredits(personId: TraktId): PersonMoviesDto
}
