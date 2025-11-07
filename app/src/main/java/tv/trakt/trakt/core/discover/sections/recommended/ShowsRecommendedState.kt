package tv.trakt.trakt.core.discover.sections.recommended

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import tv.trakt.trakt.common.helpers.LoadingState
import tv.trakt.trakt.common.model.Show

@Immutable
internal data class ShowsRecommendedState(
    val items: ImmutableList<Show>? = null,
    val loading: LoadingState = LoadingState.IDLE,
    val error: Exception? = null,
)
