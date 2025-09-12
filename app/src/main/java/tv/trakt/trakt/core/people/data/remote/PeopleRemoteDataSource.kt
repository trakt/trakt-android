package tv.trakt.trakt.core.people.data.remote

import tv.trakt.trakt.common.networking.PersonDto

internal interface PeopleRemoteDataSource {
    suspend fun getBirthdayPeople(): List<PersonDto>
}
