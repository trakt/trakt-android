package tv.trakt.trakt.app.core.home.sections.shows.upnext.model

import androidx.compose.runtime.Immutable
import tv.trakt.trakt.common.model.TraktId
import java.time.Instant

@Immutable
sealed interface ProgressItem {
    val id: TraktId
    val key: String
    val sortKey: Instant
}
