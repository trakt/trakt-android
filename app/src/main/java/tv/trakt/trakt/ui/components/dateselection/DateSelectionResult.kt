package tv.trakt.trakt.ui.components.dateselection

import java.time.Instant

private val TraktUnknownDate: Instant = Instant.parse("1970-01-01T00:00:00Z")

sealed interface DateSelectionResult

data object Now : DateSelectionResult

data object ReleaseDate : DateSelectionResult

data class CustomDate(
    val date: Instant,
) : DateSelectionResult

data object UnknownDate : DateSelectionResult {
    val date: Instant = TraktUnknownDate
}
