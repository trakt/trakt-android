package tv.trakt.app.tv.core.profile.sections.history.viewall

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import tv.trakt.app.tv.common.model.SyncHistoryItem

@Immutable
internal data class ProfileHistoryViewAllState(
    val isLoading: Boolean = false,
    val isLoadingPage: Boolean = false,
    val items: ImmutableList<SyncHistoryItem>? = null,
    val error: Exception? = null,
)
