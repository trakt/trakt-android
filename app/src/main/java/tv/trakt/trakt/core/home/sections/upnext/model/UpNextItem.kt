package tv.trakt.trakt.core.home.sections.upnext.model

import androidx.compose.runtime.Immutable
import tv.trakt.trakt.common.model.MediaType
import tv.trakt.trakt.common.model.TraktId

@Immutable
sealed interface UpNextItem {
    val id: TraktId
    val mediaId: TraktId
    val key: String
    val sortKey: String
    val type: MediaType
    val loading: Boolean
}
