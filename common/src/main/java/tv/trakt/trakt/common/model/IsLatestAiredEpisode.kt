package tv.trakt.trakt.common.model

fun isLatestAiredEpisode(
    episode: SeasonEpisode?,
    latest: SeasonEpisode?,
): Boolean {
    if (episode == null) return false
    if (latest == null) return true

    if (episode.season > latest.season) return true
    if (episode.season < latest.season) return false
    return episode.episode >= latest.episode
}
