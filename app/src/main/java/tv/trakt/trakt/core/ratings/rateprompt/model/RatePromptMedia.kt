package tv.trakt.trakt.core.ratings.rateprompt.model

import androidx.compose.runtime.Immutable
import tv.trakt.trakt.common.model.Images
import tv.trakt.trakt.common.model.MediaType
import tv.trakt.trakt.common.model.Movie
import tv.trakt.trakt.common.model.Show
import tv.trakt.trakt.common.model.TraktId

@Immutable
sealed interface RatePromptMedia {
    val id: TraktId
    val mediaType: MediaType
    val title: String
    val images: Images?

    /** Favorites are a movie-only affordance; shows never expose the heart. */
    val favorite: Boolean

    @Immutable
    data class MovieMedia(
        val movie: Movie,
        override val favorite: Boolean,
    ) : RatePromptMedia {
        override val id: TraktId get() = movie.ids.trakt
        override val mediaType: MediaType get() = MediaType.Movie
        override val title: String get() = movie.title
        override val images: Images? get() = movie.images
    }

    @Immutable
    data class ShowMedia(
        val show: Show,
    ) : RatePromptMedia {
        override val id: TraktId get() = show.ids.trakt
        override val mediaType: MediaType get() = MediaType.Show
        override val title: String get() = show.title
        override val images: Images? get() = show.images
        override val favorite: Boolean get() = false
    }
}
