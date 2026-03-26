package tv.trakt.trakt.core.lists.sheets.edit

import androidx.compose.runtime.Immutable
import tv.trakt.trakt.common.helpers.LoadingState
import tv.trakt.trakt.common.helpers.LoadingState.Idle

@Immutable
internal data class EditListState(
    val loadingEdit: LoadingState = Idle,
    val loadingDelete: LoadingState = Idle,
    val error: Exception? = null,
)
