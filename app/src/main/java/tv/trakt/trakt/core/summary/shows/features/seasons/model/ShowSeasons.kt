package tv.trakt.trakt.core.summary.shows.features.seasons.model

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import tv.trakt.trakt.common.helpers.extensions.emptyImmutableList
import tv.trakt.trakt.common.model.Episode
import tv.trakt.trakt.common.model.Season

@Immutable
internal data class ShowSeasons(
    val seasons: ImmutableList<Season> = emptyImmutableList(),
    val selectedSeason: Season? = null,
    val selectedSeasonEpisodes: ImmutableList<Episode> = emptyImmutableList(),
    val isSeasonLoading: Boolean = false,
)
