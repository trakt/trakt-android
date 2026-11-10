package tv.trakt.trakt.common.model

import androidx.compose.runtime.Immutable
import kotlinx.serialization.Serializable
import tv.trakt.trakt.common.model.SocialIds.Companion
import tv.trakt.trakt.common.networking.PersonSocialIdsDto
import tv.trakt.trakt.common.networking.SocialIdsDto

@Immutable
@Serializable
data class SocialIds(
    val twitter: String?,
    val facebook: String?,
    val instagram: String?,
    val wikipedia: String?,
    val tiktok: String? = null,
    val youtube: String? = null,
    val wikidata: String? = null,
    val wikipediaLanguage: String? = null,
) {
    companion object
}

fun Companion.fromDto(dto: SocialIdsDto): SocialIds {
    return SocialIds(
        twitter = dto.twitter,
        facebook = dto.facebook,
        instagram = dto.instagram,
        wikipedia = dto.wikipedia,
    )
}

fun Companion.fromDto(dto: PersonSocialIdsDto): SocialIds {
    return SocialIds(
        twitter = dto.twitter,
        facebook = dto.facebook,
        instagram = dto.instagram,
        wikipedia = dto.wikipedia,
        tiktok = dto.tiktok,
        youtube = dto.youtube,
        wikidata = dto.wikidata,
        wikipediaLanguage = dto.wikipediaLanguage,
    )
}
