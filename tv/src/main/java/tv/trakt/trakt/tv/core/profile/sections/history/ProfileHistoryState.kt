package tv.trakt.trakt.tv.core.profile.sections.history

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import tv.trakt.trakt.tv.common.model.SyncHistoryItem

@Immutable
internal data class ProfileHistoryState(
    val items: ImmutableList<SyncHistoryItem>? = null,
    val isLoading: Boolean = true,
    val error: Exception? = null,
)
