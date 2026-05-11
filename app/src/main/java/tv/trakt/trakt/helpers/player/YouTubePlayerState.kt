package tv.trakt.trakt.helpers.player

import androidx.compose.runtime.Immutable

@Immutable
internal data class YouTubePlayerState(
    val videoId: String? = null,
    val videoUrl: String? = null,
)
