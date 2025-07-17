package tv.trakt.trakt.tv.core.shows.model

import androidx.compose.runtime.Immutable

@Immutable
internal data class AnticipatedShow(
    val listCount: Int,
    val show: Show,
)
