package tv.trakt.trakt.core.settings.features.younify.model

import androidx.annotation.StringRes
import tv.trakt.trakt.resources.R
import tv.younify.sdk.connect.StreamingService

internal val StreamingService.linkStatus: LinkStatus
    get() {
        return when {
            link == null -> LinkStatus.UNLINKED
            link?.isBroken == true -> LinkStatus.BROKEN
            else -> LinkStatus.LINKED
        }
    }

internal enum class LinkStatus(
    @param:StringRes val displayTextRes: Int,
) {
    LINKED(R.string.text_younify_status_linked),
    UNLINKED(R.string.text_younify_status_unlinked),
    BROKEN(R.string.text_younify_status_broken),
}
