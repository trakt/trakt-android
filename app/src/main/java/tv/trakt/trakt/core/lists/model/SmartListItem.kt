package tv.trakt.trakt.core.lists.model

import androidx.compose.runtime.Immutable
import tv.trakt.trakt.common.model.Images
import tv.trakt.trakt.common.model.MediaType
import tv.trakt.trakt.common.model.Movie
import tv.trakt.trakt.common.model.Show
import tv.trakt.trakt.common.model.TraktId

@Immutable
internal sealed interface SmartListItem {
    val rank: Int

    @Immutable
    data class MovieItem(
        override val rank: Int,
        val movie: Movie,
    ) : SmartListItem

    @Immutable
    data class ShowItem(
        override val rank: Int,
        val show: Show,
    ) : SmartListItem

    val id: TraktId
        get() = when (this) {
            is MovieItem -> movie.ids.trakt
            is ShowItem -> show.ids.trakt
        }

    val type: MediaType
        get() = when (this) {
            is MovieItem -> MediaType.Movie
            is ShowItem -> MediaType.Show
        }

    val key: String
        get() = "${id.value}-${type.value}-smart"

    val images: Images?
        get() = when (this) {
            is MovieItem -> movie.images
            is ShowItem -> show.images
        }
}
