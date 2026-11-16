package tv.trakt.trakt.app.core.details.lists.manage.navigation

import androidx.navigation.NavController
import tv.trakt.trakt.app.core.details.lists.details.media.navigation.navigateToCustomListMedia
import tv.trakt.trakt.app.core.lists.details.personal.navigation.navigateToPersonalList
import tv.trakt.trakt.common.model.lists.CustomList

internal fun NavController.navigateToUserList(
    list: CustomList,
    isOwner: Boolean,
) {
    when {
        isOwner -> navigateToPersonalList(list)
        else -> navigateToCustomListMedia(list, null)
    }
}
