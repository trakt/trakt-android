package tv.trakt.trakt.common.core.search.model

import androidx.compose.runtime.Immutable

@Immutable
data class SearchInput(
    val query: String = "",
    val filter: SearchFilter = SearchFilter.MEDIA,
)
