package tv.trakt.trakt.app.common.model

import androidx.compose.runtime.Immutable

@Immutable
internal data class CastPerson(
    val characters: List<String>,
    val person: Person,
)
