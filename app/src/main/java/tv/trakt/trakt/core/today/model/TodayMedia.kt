package tv.trakt.trakt.core.today.model

import androidx.compose.runtime.Immutable
import tv.trakt.trakt.common.model.Ids
import tv.trakt.trakt.common.model.Images
import tv.trakt.trakt.common.model.MediaType
import tv.trakt.trakt.common.model.Movie
import tv.trakt.trakt.common.model.Show

@Immutable
internal sealed interface TodayMedia {
    val ids: Ids
    val type: MediaType
    val title: String
    val images: Images?

    val key: String
        get() = "${type.value}-${ids.trakt.value}"

    @Immutable
    data class MovieMedia(
        val movie: Movie,
    ) : TodayMedia {
        override val ids = movie.ids
        override val type = MediaType.Movie
        override val title = movie.title
        override val images = movie.images
    }

    @Immutable
    data class ShowMedia(
        val show: Show,
    ) : TodayMedia {
        override val ids = show.ids
        override val type = MediaType.Show
        override val title = show.title
        override val images = show.images
    }
}
