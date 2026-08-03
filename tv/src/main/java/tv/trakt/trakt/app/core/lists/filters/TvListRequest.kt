package tv.trakt.trakt.app.core.lists.filters

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import tv.trakt.trakt.common.model.globalfilter.GlobalFilter
import tv.trakt.trakt.common.model.sorting.Sorting

@Immutable
internal data class TvListRequest(
    val page: Int,
    val limit: Int,
    val filter: GlobalFilter,
    val sorting: Sorting,
) {
    init {
        require(page > 0) { "Page must be greater than zero" }
        require(limit > 0) { "Limit must be greater than zero" }
    }
}

@Immutable
internal data class TvListPage<T>(
    val items: ImmutableList<T>,
    val nextPage: Int,
    val hasMore: Boolean,
)
