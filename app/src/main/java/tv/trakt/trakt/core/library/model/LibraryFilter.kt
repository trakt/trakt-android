package tv.trakt.trakt.core.library.model

import androidx.annotation.StringRes
import tv.trakt.trakt.resources.R

enum class LibraryFilter(
    val value: String,
    @param:StringRes val displayRes: Int,
) {
    CUSTOM("custom", R.string.translated_value_library_custom),
    PLEX("plex", R.string.translated_value_library_plex),
}
