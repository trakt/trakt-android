package tv.trakt.trakt.app.core.details.show.models

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import tv.trakt.trakt.common.helpers.extensions.EmptyImmutableList
import tv.trakt.trakt.common.model.Episode
import tv.trakt.trakt.common.model.Season

@Immutable
internal data class ShowSeasons(
    val seasons: ImmutableList<SeasonItem> = EmptyImmutableList,
    val selectedSeason: SeasonItem? = null,
    val selectedSeasonEpisodes: ImmutableList<EpisodeItem> = EmptyImmutableList,
    val isSeasonLoading: Boolean = false,
) {
    @Immutable
    data class SeasonItem(
        val season: Season,
        val watching: Boolean = false,
        val watched: Boolean = false,
    )

    @Immutable
    data class EpisodeItem(
        val episode: Episode,
        val watched: Boolean = false,
    )
}
