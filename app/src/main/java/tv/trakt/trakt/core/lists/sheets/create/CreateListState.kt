package tv.trakt.trakt.core.lists.sheets.create

import androidx.compose.runtime.Immutable
import tv.trakt.trakt.common.helpers.LoadingState
import tv.trakt.trakt.common.helpers.LoadingState.Idle

@Immutable
internal data class CreateListState(
    val loading: LoadingState = Idle,
    val error: Exception? = null,
    val listsLimitError: Exception? = null,
)
