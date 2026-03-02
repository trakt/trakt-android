package tv.trakt.trakt.app.core.home.sections.history

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import tv.trakt.trakt.app.common.model.SyncHistoryItem

@Immutable
internal data class HomeHistoryState(
    val items: ImmutableList<SyncHistoryItem>? = null,
    val isLoading: Boolean = true,
    val error: Exception? = null,
)
