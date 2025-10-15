package tv.trakt.trakt.core.summary.shows.features.seasons

import androidx.compose.runtime.Immutable
import tv.trakt.trakt.common.helpers.LoadingState
import tv.trakt.trakt.common.model.Show
import tv.trakt.trakt.core.summary.shows.features.seasons.model.ShowSeasons

@Immutable
internal data class ShowSeasonsState(
    val show: Show? = null,
    val items: ShowSeasons = ShowSeasons(),
    val loading: LoadingState = LoadingState.IDLE,
    val error: Exception? = null,
)
