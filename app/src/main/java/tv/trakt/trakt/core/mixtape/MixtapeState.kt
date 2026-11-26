package tv.trakt.trakt.core.mixtape

import androidx.compose.runtime.Immutable
import tv.trakt.trakt.common.model.MediaType

@Immutable
internal data class MixtapeState(
    val type: MediaType = MediaType.Movie,
)
