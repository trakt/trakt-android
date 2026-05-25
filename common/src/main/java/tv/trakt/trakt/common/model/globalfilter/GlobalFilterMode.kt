package tv.trakt.trakt.common.model.globalfilter

import androidx.annotation.StringRes
import tv.trakt.trakt.resources.R

enum class GlobalFilterMode(
    @param:StringRes val displayStringRes: Int,
) {
    Simple(R.string.text_simple),
    Advanced(R.string.text_advanced),
}
