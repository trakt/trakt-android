package tv.trakt.trakt.common.model.reactions

import androidx.compose.runtime.Immutable
import tv.trakt.trakt.common.networking.api.v3.model.reactions.V3UserMediaReaction

@Immutable
data class UserMediaReaction(
    val id: Long,
    val reaction: MediaReaction,
) {
    companion object {
        fun fromDto(dto: V3UserMediaReaction): UserMediaReaction? {
            val reaction = MediaReaction.fromValue(dto.reaction.type) ?: return null

            return UserMediaReaction(
                id = dto.id,
                reaction = reaction,
            )
        }
    }
}
