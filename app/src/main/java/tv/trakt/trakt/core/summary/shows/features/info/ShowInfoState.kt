package tv.trakt.trakt.core.summary.shows.features.info

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import tv.trakt.trakt.common.helpers.LoadingState
import tv.trakt.trakt.common.helpers.LoadingState.Idle
import tv.trakt.trakt.common.model.Show
import tv.trakt.trakt.common.networking.ShowStatsDto
import tv.trakt.trakt.core.summary.shows.features.info.usecase.GetShowCrewUseCase

@Immutable
internal data class ShowInfoState(
    val show: Show? = null,
    val showStats: ShowStatsDto? = null,
    val showStudios: ImmutableList<String>? = null,
    val showCrew: GetShowCrewUseCase.Result? = null,
    val loading: LoadingState = Idle,
    val error: Exception? = null,
)
