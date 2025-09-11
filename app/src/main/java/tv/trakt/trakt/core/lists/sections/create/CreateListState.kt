package tv.trakt.trakt.core.lists.sections.create

import androidx.compose.runtime.Immutable
import tv.trakt.trakt.common.helpers.LoadingState
import tv.trakt.trakt.common.helpers.LoadingState.IDLE

@Immutable
internal data class CreateListState(
    val loading: LoadingState = IDLE,
    val error: Exception? = null,
    val listsLimitError: Exception? = null,
)
