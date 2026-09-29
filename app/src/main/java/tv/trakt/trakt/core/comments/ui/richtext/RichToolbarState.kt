package tv.trakt.trakt.core.comments.ui.richtext

import androidx.compose.runtime.Immutable

@Immutable
internal data class RichToolbarState(
    val bold: Boolean = false,
    val italic: Boolean = false,
    val spoiler: Boolean = false,
    val mention: Boolean = false,
    val bulletList: Boolean = false,
    val quote: Boolean = false,
)
