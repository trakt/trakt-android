package tv.trakt.trakt.core.search.model

import androidx.compose.runtime.Immutable
import tv.trakt.trakt.common.model.Images

@Immutable
internal sealed class SearchItem(
    open val rank: Long,
) {
    @Immutable
    internal data class Movie(
        override val rank: Long,
        val movie: tv.trakt.trakt.common.model.Movie,
    ) : SearchItem(rank)

    @Immutable
    internal data class
    Show(
        override val rank: Long,
        val show: tv.trakt.trakt.common.model.Show,
    ) : SearchItem(rank)

    val key: String
        get() = when (this) {
            is Movie -> "movie_${movie.ids.trakt.value}"
            is Show -> "show_${show.ids.trakt.value}"
        }

    val images: Images?
        get() = when (this) {
            is Movie -> movie.images
            is Show -> show.images
        }
}
