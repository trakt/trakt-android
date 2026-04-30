package tv.trakt.trakt.common.model

import androidx.compose.runtime.Immutable
import kotlinx.serialization.Serializable

@Immutable
@Serializable
data class MovieProgress(
    val ids: Ids,
)
