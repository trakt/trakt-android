package tv.trakt.trakt.core.profile.sections.library.model

import androidx.annotation.StringRes
import tv.trakt.trakt.resources.R

enum class LibraryFilter(
    val value: String,
    @param:StringRes val displayRes: Int,
) {
    CUSTOM("other", R.string.translated_value_library_custom),
    PLEX("plex", R.string.translated_value_library_plex),
}
