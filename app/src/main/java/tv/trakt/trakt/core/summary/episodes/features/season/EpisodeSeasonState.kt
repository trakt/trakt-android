package tv.trakt.trakt.core.summary.episodes.features.season

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import tv.trakt.trakt.common.helpers.LoadingState
import tv.trakt.trakt.common.helpers.StringResource
import tv.trakt.trakt.common.helpers.extensions.EmptyImmutableList
import tv.trakt.trakt.common.model.Show
import tv.trakt.trakt.core.summary.shows.features.seasons.model.EpisodeItem

@Immutable
internal data class EpisodeSeasonState(
    val show: Show? = null,
    val episodes: ImmutableList<EpisodeItem> = EmptyImmutableList,
    val seasonNumber: Int? = null,
    val loading: LoadingState = LoadingState.IDLE,
    val loadingEpisode: LoadingState = LoadingState.IDLE,
    val info: StringResource? = null,
    val error: Exception? = null,
)
