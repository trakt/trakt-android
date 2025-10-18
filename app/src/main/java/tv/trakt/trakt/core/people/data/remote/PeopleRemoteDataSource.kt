package tv.trakt.trakt.core.people.data.remote

import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.networking.PersonDto
import tv.trakt.trakt.common.networking.PersonMoviesDto
import tv.trakt.trakt.common.networking.PersonShowsDto

internal interface PeopleRemoteDataSource {
    suspend fun getPersonDetails(personId: TraktId): PersonDto

    suspend fun getPersonShowsCredits(personId: TraktId): PersonShowsDto

    suspend fun getPersonMoviesCredits(personId: TraktId): PersonMoviesDto

    suspend fun getBirthdayPeople(): List<PersonDto>
}
