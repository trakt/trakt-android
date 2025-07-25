package tv.trakt.trakt.tv.core.people.data.remote

import tv.trakt.trakt.common.networking.PersonDetailsDto
import tv.trakt.trakt.common.networking.PersonMoviesDto
import tv.trakt.trakt.common.networking.PersonShowsDto
import tv.trakt.trakt.tv.common.model.TraktId

internal interface PeopleRemoteDataSource {
    suspend fun getPersonDetails(personId: TraktId): PersonDetailsDto

    suspend fun getPersonShowsCredits(personId: TraktId): PersonShowsDto

    suspend fun getPersonMoviesCredits(personId: TraktId): PersonMoviesDto
}
