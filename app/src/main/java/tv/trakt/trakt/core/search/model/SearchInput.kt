package tv.trakt.trakt.core.search.model

import androidx.compose.runtime.Immutable

@Immutable
internal data class SearchInput(
    val query: String = "",
    val filter: SearchFilter = SearchFilter.MEDIA,
)
