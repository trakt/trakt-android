package tv.trakt.trakt.common.model.reactions

import androidx.compose.runtime.Immutable
import tv.trakt.trakt.common.model.MediaType
import tv.trakt.trakt.common.model.TraktId

@Immutable
data class MediaReactionsTarget(
    val type: MediaType,
    val id: TraktId,
)
