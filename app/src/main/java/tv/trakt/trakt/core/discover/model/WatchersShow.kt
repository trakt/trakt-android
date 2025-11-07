package tv.trakt.trakt.core.discover.model

import androidx.compose.runtime.Immutable
import tv.trakt.trakt.common.model.Show

@Immutable
internal data class WatchersShow(
    val watchers: Int,
    val show: Show,
)
