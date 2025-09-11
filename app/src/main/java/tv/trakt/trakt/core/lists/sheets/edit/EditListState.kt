package tv.trakt.trakt.core.lists.sheets.edit

import androidx.compose.runtime.Immutable
import tv.trakt.trakt.common.helpers.LoadingState
import tv.trakt.trakt.common.helpers.LoadingState.IDLE

@Immutable
internal data class EditListState(
    val loadingEdit: LoadingState = IDLE,
    val loadingDelete: LoadingState = IDLE,
    val error: Exception? = null,
)
