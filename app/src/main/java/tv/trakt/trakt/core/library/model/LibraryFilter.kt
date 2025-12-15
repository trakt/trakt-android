package tv.trakt.trakt.core.library.model

import androidx.annotation.StringRes
import tv.trakt.trakt.resources.R

enum class LibraryFilter(
    val value: String,
    @param:StringRes val displayRes: Int,
) {
    CUSTOM("custom", R.string.text_sort_custom),
    PLEX("plex", R.string.text_sort_plex),
}
