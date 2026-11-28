package tv.trakt.trakt.core.mixtape

import kotlinx.collections.immutable.ImmutableList
import tv.trakt.trakt.common.model.MediaType
import tv.trakt.trakt.common.model.mixtape.MixtapeItem

internal sealed interface MixtapeEvent {
    data class Mixed(
        val type: MediaType,
        val picks: ImmutableList<MixtapeItem>,
    ) : MixtapeEvent
}
