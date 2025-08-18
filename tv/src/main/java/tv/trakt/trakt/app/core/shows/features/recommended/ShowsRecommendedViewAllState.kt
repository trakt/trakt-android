package tv.trakt.trakt.app.core.shows.features.recommended

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import tv.trakt.trakt.common.model.Show

@Immutable
internal data class ShowsRecommendedViewAllState(
    val isLoading: Boolean = false,
    val isLoadingPage: Boolean = false,
    val shows: ImmutableList<Show>? = null,
    val error: Exception? = null,
)
