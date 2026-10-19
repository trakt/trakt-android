package tv.trakt.trakt.common.model.profile

import androidx.compose.runtime.Immutable
import tv.trakt.trakt.common.model.MediaType
import tv.trakt.trakt.common.model.Show
import tv.trakt.trakt.common.model.TraktId
import java.time.Instant

@Immutable
sealed interface ProfileProgressItem {
    @Immutable
    data class ShowItem(
        val show: Show,
        val hiddenAt: Instant? = null,
        val remainingEpisodes: Int? = null,
    ) : ProfileProgressItem

    val id: TraktId
        get() = when (this) {
            is ShowItem -> show.ids.trakt
        }

    val key: String
        get() = when (this) {
            is ShowItem -> "${show.ids.trakt.value}-progress-show"
        }

    val type: MediaType
        get() = when (this) {
            is ShowItem -> MediaType.Show
        }
}
