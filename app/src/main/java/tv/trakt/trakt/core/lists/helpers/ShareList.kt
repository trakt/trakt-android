package tv.trakt.trakt.core.lists.helpers

import android.content.Context
import android.content.Intent
import timber.log.Timber
import tv.trakt.trakt.common.Config
import tv.trakt.trakt.common.helpers.extensions.toShareUrl
import tv.trakt.trakt.common.model.User

internal fun shareList(
    user: User?,
    list: String,
    context: Context,
) {
    if (user == null) {
        Timber.e("Unable to share: user is null")
        return
    }

    val intent = Intent().apply {
        action = Intent.ACTION_SEND
        putExtra(
            Intent.EXTRA_TEXT,
            Config.webListUrl(
                userId = user.ids.slug.value,
                listId = list,
            ).toShareUrl(user.settings?.shareCode),
        )
        type = "text/plain"
    }

    context.startActivity(Intent.createChooser(intent, user.displayName))
}
