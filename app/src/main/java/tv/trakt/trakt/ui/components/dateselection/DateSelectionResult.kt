package tv.trakt.trakt.ui.components.dateselection

import tv.trakt.trakt.common.helpers.extensions.nowUtcInstant
import java.time.Instant

sealed interface DateSelectionResult {
    val dateString: String
    val analyticsStrings: String
}

data object Now : DateSelectionResult {
    override val dateString: String
        get() = nowUtcInstant().toString()
    override val analyticsStrings: String = "now"
}

data object ReleaseDate : DateSelectionResult {
    override val dateString: String = "released"
    override val analyticsStrings: String = "released"
}

data object UnknownDate : DateSelectionResult {
    // Maps to 1970-01-01T00:00:00Z in Trakt
    override val dateString: String = "unknown"
    override val analyticsStrings: String = "unknown"
}

data class CustomDate(
    val date: Instant,
) : DateSelectionResult {
    override val dateString: String
        get() = date.toString()
    override val analyticsStrings: String = "custom"
}
