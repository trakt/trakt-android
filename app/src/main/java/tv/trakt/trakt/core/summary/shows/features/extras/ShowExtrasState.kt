package tv.trakt.trakt.core.summary.shows.features.extras

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import tv.trakt.trakt.common.helpers.LoadingState
import tv.trakt.trakt.common.model.ExtraVideo

@Immutable
internal data class ShowExtrasState(
    val items: ImmutableList<ExtraVideo>? = null,
    val filters: FiltersState = FiltersState(),
    val loading: LoadingState = LoadingState.IDLE,
    val error: Exception? = null,
    val collapsed: Boolean? = null,
) {
    data class FiltersState(
        val filters: ImmutableList<String> = emptyList<String>().toImmutableList(),
        val selectedFilter: String? = null,
    )
}
