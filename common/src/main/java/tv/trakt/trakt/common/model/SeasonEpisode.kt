package tv.trakt.trakt.common.model

import androidx.compose.runtime.Immutable
import kotlinx.serialization.Serializable
import java.util.Locale.ROOT

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

    override fun toString(): String {
        return String.format(
            ROOT,
            "%01dx%02d",
            this.season,
            this.episode,
        )
    }
}
