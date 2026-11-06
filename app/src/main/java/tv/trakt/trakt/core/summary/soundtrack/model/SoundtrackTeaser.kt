package tv.trakt.trakt.core.summary.soundtrack.model

import kotlinx.collections.immutable.persistentListOf
import tv.trakt.trakt.common.model.soundtrack.SoundtrackTrack

/**
 * Sample rows behind the VIP upsell. They show the shape of the section without
 * implying anything about the title being viewed, and never become interactive.
 */
internal val SoundtrackTeaserTracks = persistentListOf(
    teaserTrack(title = "Main Title", position = 0, playable = true),
    teaserTrack(title = "Opening Credits", position = 1, playable = true),
    teaserTrack(title = "End Credits", position = 2, playable = false),
)

private fun teaserTrack(
    title: String,
    position: Int,
    playable: Boolean,
) = SoundtrackTrack(
    title = title,
    performer = null,
    spotifyId = if (playable) "teaser" else null,
    matchedOn = null,
    position = position,
    season = null,
)
