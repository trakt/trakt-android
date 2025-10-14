package tv.trakt.trakt.app.core.details.show.models

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import tv.trakt.trakt.common.model.Episode
import tv.trakt.trakt.common.model.Season

@Immutable
internal data class ShowSeasons(
    val seasons: ImmutableList<Season> = emptyList<Season>().toImmutableList(),
    val selectedSeason: Season? = null,
    val selectedSeasonEpisodes: ImmutableList<Episode> = emptyList<Episode>().toImmutableList(),
    val isSeasonLoading: Boolean = false,
)
