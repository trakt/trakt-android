package tv.trakt.trakt.common.networking.api.imdb.model

import kotlinx.serialization.Serializable

@Serializable
data class ImdbLookupItemDto(
    val type: String,
    val movie: ImdbLookupMediaDto? = null,
    val show: ImdbLookupMediaDto? = null,
    val episode: ImdbLookupEpisodeDto? = null,
    val person: ImdbLookupMediaDto? = null,
)

@Serializable
data class ImdbLookupMediaDto(
    val ids: ImdbLookupIdsDto,
)

@Serializable
data class ImdbLookupEpisodeDto(
    val season: Int,
    val number: Int,
    val ids: ImdbLookupIdsDto,
)

@Serializable
data class ImdbLookupIdsDto(
    val trakt: Int,
)
