package tv.trakt.trakt.core.reactions.media.data

import androidx.compose.runtime.Immutable
import tv.trakt.trakt.core.reactions.media.MediaReactionEmoji

@Immutable
internal data class MediaReaction(
    val id: Long,
    val count: Int,
    val emoji: MediaReactionEmoji,
)
