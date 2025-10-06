package tv.trakt.trakt.common.model

import androidx.compose.runtime.Immutable

@Immutable
data class CastPerson(
    val characters: List<String>,
    val person: Person,
)
