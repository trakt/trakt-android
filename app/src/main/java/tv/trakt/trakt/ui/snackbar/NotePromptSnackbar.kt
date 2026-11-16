package tv.trakt.trakt.ui.snackbar

import android.content.res.Resources
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import tv.trakt.trakt.resources.R

internal suspend fun SnackbarHostState.showFavoriteNotePrompt(
    resources: Resources,
    onAddNote: () -> Unit,
) {
    val result = showSnackbar(
        message = resources.getString(R.string.text_info_favorites_added),
        actionLabel = resources.getString(R.string.button_text_add_note),
        duration = SnackbarDuration.Long,
    )

    if (result == SnackbarResult.ActionPerformed) {
        onAddNote()
    }
}
