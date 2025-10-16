package tv.trakt.trakt.core.summary.shows.features.seasons.model

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import tv.trakt.trakt.common.helpers.extensions.EmptyImmutableList
import tv.trakt.trakt.common.model.Season

@Immutable
internal data class ShowSeasons(
    val seasons: ImmutableList<Season> = EmptyImmutableList,
    val selectedSeason: Season? = null,
    val selectedSeasonEpisodes: ImmutableList<EpisodeItem> = EmptyImmutableList,
    val isSeasonLoading: Boolean = false,
)
