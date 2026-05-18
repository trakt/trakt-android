package tv.trakt.trakt.common.model

fun isLatestAiredEpisode(
    episode: SeasonEpisode?,
    latest: SeasonEpisode?,
): Boolean {
    if (episode == null) return false
    if (latest == null) return true
    return episode.id >= latest.id
}
