package tv.trakt.trakt.common.model

import androidx.compose.runtime.Immutable

@Immutable
data class CastPerson(
    val person: Person,
    val characters: List<String>,
    val episodesCount: Int = 0,
)
