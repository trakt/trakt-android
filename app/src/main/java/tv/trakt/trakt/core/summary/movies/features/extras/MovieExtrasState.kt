package tv.trakt.trakt.core.summary.movies.features.extras

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import tv.trakt.trakt.common.helpers.LoadingState
import tv.trakt.trakt.common.model.ExtraVideo

@Immutable
internal data class MovieExtrasState(
    val items: ImmutableList<ExtraVideo>? = null,
    val filters: FiltersState = FiltersState(),
    val loading: LoadingState = LoadingState.IDLE,
    val collapsed: Boolean? = null,
    val error: Exception? = null,
) {
    data class FiltersState(
        val filters: ImmutableList<String> = emptyList<String>().toImmutableList(),
        val selectedFilter: String? = null,
    )
}
