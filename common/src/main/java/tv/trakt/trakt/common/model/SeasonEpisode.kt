package tv.trakt.trakt.common.model

import androidx.compose.runtime.Immutable
import kotlinx.serialization.Serializable

@Immutable
@Serializable
data class SeasonEpisode(
    val season: Int,
    val episode: Int,
    val id: Long = (season * 10_000L) + episode,
) {
    init {
        require(season >= 0) { "Season number must be >= 0" }
        require(episode > 0) { "Episode number must be > 0" }
    }
}
