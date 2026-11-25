package tv.trakt.trakt.core.lists.sheets.collaborators

import android.content.res.Resources
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import tv.trakt.trakt.core.lists.sections.personal.model.PersonalListType
import tv.trakt.trakt.core.lists.sections.personal.model.PersonalListType.Collaborations
import tv.trakt.trakt.resources.R

/**
 * @return true when the user tapped the action to open the section the list moved to.
 */
internal suspend fun SnackbarHostState.showListMovedSnackbar(
    resources: Resources,
    listName: String,
    destination: PersonalListType,
    viewAction: Boolean = true,
): Boolean {
    val message = when (destination) {
        Collaborations -> R.string.action_toast_list_moved_to_shared
        else -> R.string.action_toast_list_moved_to_personal
    }

    val result = showSnackbar(
        message = resources.getString(message, listName),
        actionLabel = when {
            viewAction -> resources.getString(R.string.action_toast_action_view_lists)
            else -> null
        },
        duration = SnackbarDuration.Long,
    )

    return result == SnackbarResult.ActionPerformed
}
