package tv.trakt.trakt.core.summary.episodes.features.comments

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import tv.trakt.trakt.common.helpers.LoadingState
import tv.trakt.trakt.common.model.Comment

@Immutable
internal data class EpisodeCommentsState(
    val items: ImmutableList<Comment>? = null,
    val loading: LoadingState = LoadingState.IDLE,
    val error: Exception? = null,
)
