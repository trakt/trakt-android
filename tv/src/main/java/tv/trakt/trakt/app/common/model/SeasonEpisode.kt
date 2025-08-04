package tv.trakt.trakt.app.common.model

import androidx.compose.runtime.Immutable
import kotlinx.serialization.Serializable

@Immutable
@Serializable
internal data class SeasonEpisode(
    val season: Int,
    val episode: Int,
) {
    init {
        require(season >= 0) { "Season number must be >= 0" }
        require(episode > 0) { "Episode number must be > 0" }
    }
}
