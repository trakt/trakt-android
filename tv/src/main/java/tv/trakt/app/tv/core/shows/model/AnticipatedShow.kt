package tv.trakt.app.tv.core.shows.model

import androidx.compose.runtime.Immutable

@Immutable
internal data class AnticipatedShow(
    val listCount: Int,
    val show: Show,
)
