package tv.trakt.trakt.common.model

import androidx.compose.runtime.Immutable
import kotlinx.serialization.Serializable
import tv.trakt.trakt.common.model.SocialIds.Companion
import tv.trakt.trakt.common.networking.SocialIdsDto

@Immutable
@Serializable
data class SocialIds(
    val twitter: String?,
    val facebook: String?,
    val instagram: String?,
    val wikipedia: String?,
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
