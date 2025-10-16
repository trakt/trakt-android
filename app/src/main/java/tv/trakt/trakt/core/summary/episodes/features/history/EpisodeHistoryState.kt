package tv.trakt.trakt.core.summary.episodes.features.history

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import tv.trakt.trakt.common.helpers.LoadingState
import tv.trakt.trakt.core.home.sections.activity.model.HomeActivityItem

@Immutable
internal data class EpisodeHistoryState(
    val items: ImmutableList<HomeActivityItem.EpisodeItem>? = null,
    val loading: LoadingState = LoadingState.IDLE,
    val error: Exception? = null,
)
