package tv.trakt.trakt.ui.components.dateselection

import tv.trakt.trakt.common.helpers.extensions.nowUtcInstant
import java.time.Instant

sealed interface DateSelectionResult {
    val dateString: String
        get() = when (this) {
            is Now -> nowUtcInstant().toString()
            is CustomDate -> date.toString()
            is ReleaseDate -> "released"
            is UnknownDate -> "unknown" // Maps to 1970-01-01T00:00:00Z in Trakt
        }

    val analyticsStrings: String
        get() = when (this) {
            is Now -> "now"
            is CustomDate -> "custom"
            is ReleaseDate -> "released"
            is UnknownDate -> "unknown"
        }
}

data object Now : DateSelectionResult

data object ReleaseDate : DateSelectionResult

data object UnknownDate : DateSelectionResult

data class CustomDate(
    val date: Instant,
) : DateSelectionResult
