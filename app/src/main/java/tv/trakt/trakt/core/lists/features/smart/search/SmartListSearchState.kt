package tv.trakt.trakt.core.lists.features.smart.search

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import tv.trakt.trakt.common.helpers.LoadingState

internal const val MIN_SEARCH_QUERY_LENGTH = 2

@Immutable
internal data class SmartListSearchState(
    val query: String = "",
    val results: ImmutableList<SmartListSearchOption> = persistentListOf(),
    val loading: LoadingState = LoadingState.Idle,
    val error: Exception? = null,
) {
    val isQueryTooShort: Boolean
        get() = query.trim().length < MIN_SEARCH_QUERY_LENGTH
}
