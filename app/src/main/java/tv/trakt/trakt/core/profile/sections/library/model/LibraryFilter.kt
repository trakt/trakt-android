package tv.trakt.trakt.core.profile.sections.library.model

import androidx.annotation.StringRes
import tv.trakt.trakt.resources.R

enum class LibraryFilter(
    val value: String,
    val requestValue: String,
    @param:StringRes val displayRes: Int,
) {
    CUSTOM("other", "other", R.string.translated_value_library_custom),
    PLEX("plex", "digital", R.string.translated_value_library_digital),
}
