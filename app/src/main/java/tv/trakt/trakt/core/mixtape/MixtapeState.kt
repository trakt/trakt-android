package tv.trakt.trakt.core.mixtape

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import tv.trakt.trakt.common.model.MediaType
import tv.trakt.trakt.common.model.mixtape.MixtapeItem
import tv.trakt.trakt.core.mixtape.model.MixtapePicker

internal const val MAX_MIXTAPE_PICKS = 5

@Immutable
internal data class MixtapeState(
    val type: MediaType = MediaType.Movie,
    val picks: ImmutableList<MixtapeItem> = persistentListOf(),
    val picker: MixtapePicker = MixtapePicker(),
    val loading: Boolean = true,
    val settling: Boolean = false,
) {
    val isFull: Boolean
        get() = picks.size >= MAX_MIXTAPE_PICKS
}
