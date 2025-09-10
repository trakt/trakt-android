package tv.trakt.trakt.common.model

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.res.stringResource
import kotlinx.serialization.Serializable
import tv.trakt.trakt.resources.R

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

    @Composable
    fun toDisplayString(): String {
        return stringResource(
            R.string.episode_footer_season_episode,
            this.season,
            this.episode,
        )
    }
}
