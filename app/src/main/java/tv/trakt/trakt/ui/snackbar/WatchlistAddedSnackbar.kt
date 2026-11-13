package tv.trakt.trakt.ui.snackbar

import android.content.res.Resources
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import tv.trakt.trakt.resources.R

internal suspend fun SnackbarHostState.showWatchlistAdded(
    resources: Resources,
    title: String,
    onChangeList: () -> Unit,
) {
    val result = showSnackbar(
        message = resources.getString(R.string.action_toast_added_to_watchlist, title),
        actionLabel = resources.getString(R.string.action_toast_action_change_list),
        duration = SnackbarDuration.Long,
    )

    if (result == SnackbarResult.ActionPerformed) {
        onChangeList()
    }
}
