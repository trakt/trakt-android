package tv.trakt.trakt.common.model.globalfilter

import androidx.annotation.StringRes
import tv.trakt.trakt.resources.R

enum class GlobalFilterMode(
    @param:StringRes val displayStringRes: Int,
) {
    Simple(R.string.tab_text_simple_filters),
    Advanced(R.string.tab_text_advanced_filters),
}
