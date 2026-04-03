package tv.trakt.trakt.core.summary.episodes.features.info

import androidx.compose.runtime.Immutable
import tv.trakt.trakt.common.helpers.LoadingState
import tv.trakt.trakt.common.model.Episode
import tv.trakt.trakt.common.model.Show
import tv.trakt.trakt.common.networking.EpisodeStatsDto
import tv.trakt.trakt.core.summary.episodes.features.info.usecase.GetEpisodeCrewUseCase

@Immutable
internal data class EpisodeInfoState(
    val show: Show? = null,
    val episode: Episode? = null,
    val episodeStats: EpisodeStatsDto? = null,
    val episodeCrew: GetEpisodeCrewUseCase.Result? = null,
    val loading: LoadingState = LoadingState.Idle,
    val error: Exception? = null,
)
