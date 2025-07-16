package tv.trakt.app.tv.common.model

import androidx.compose.runtime.Immutable

@Immutable
internal data class CastPerson(
    val characters: List<String>,
    val person: Person,
)
