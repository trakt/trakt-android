package tv.trakt.trakt.core.lists.sections.watchlist.model

import androidx.compose.runtime.Immutable
import tv.trakt.trakt.common.model.Images
import tv.trakt.trakt.common.model.Movie
import tv.trakt.trakt.common.model.Show
import tv.trakt.trakt.common.model.TraktId

@Immutable
internal sealed class WatchlistItem {
    @Immutable
    internal data class MovieItem(
        val movie: Movie,
    ) : WatchlistItem()

    @Immutable
    internal data class ShowItem(
        val show: Show,
    ) : WatchlistItem()

    val id: TraktId
        get() = when (this) {
            is ShowItem -> show.ids.trakt
            is MovieItem -> movie.ids.trakt
        }

    val key: String
        get() = when (this) {
            is ShowItem -> "${show.ids.trakt.value}-show"
            is MovieItem -> "${movie.ids.trakt.value}-movie"
        }

    val images: Images?
        get() = when (this) {
            is ShowItem -> show.images
            is MovieItem -> movie.images
        }
}
