package tv.trakt.trakt.core.home.sections.upnext.model

import androidx.compose.runtime.Immutable
import tv.trakt.trakt.common.model.TraktId
import java.time.Instant

@Immutable
sealed interface UpNextItem {
    val id: TraktId
    val key: String
    val sortKey: Instant
    val loading: Boolean
}
