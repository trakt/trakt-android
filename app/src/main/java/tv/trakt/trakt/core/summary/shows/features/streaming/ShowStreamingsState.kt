package tv.trakt.trakt.core.summary.shows.features.streaming

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import tv.trakt.trakt.common.helpers.LoadingState
import tv.trakt.trakt.common.model.streamings.StreamingService
import tv.trakt.trakt.common.model.streamings.StreamingType

@Immutable
internal data class ShowStreamingsState(
    val items: ImmutableList<Pair<StreamingService, StreamingType>>? = null,
    val loading: LoadingState = LoadingState.IDLE,
    val error: Exception? = null,
)
