package tv.trakt.trakt.common.data.dto

import kotlinx.serialization.Serializable
import tv.trakt.trakt.common.networking.PersonDto

@Serializable
data class ListFeaturedPeopleDto(
    val featured: List<PersonDto>? = null,
)
