package tv.trakt.trakt.core.summary.shows.features.seasons.model

import androidx.compose.runtime.Immutable
import tv.trakt.trakt.common.model.Episode

@Immutable
internal data class EpisodeItem(
    val episode: Episode,
    val isWatched: Boolean = false,
    val isLoading: Boolean = false,
    val isCheckable: Boolean = false,
)
