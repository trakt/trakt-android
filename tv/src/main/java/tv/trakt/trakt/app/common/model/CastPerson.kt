package tv.trakt.trakt.app.common.model

import androidx.compose.runtime.Immutable
import tv.trakt.trakt.common.model.Person

@Immutable
internal data class CastPerson(
    val characters: List<String>,
    val person: Person,
)
