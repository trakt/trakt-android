package tv.trakt.trakt.core.search.model

import androidx.compose.runtime.Immutable
import tv.trakt.trakt.common.model.Images
import tv.trakt.trakt.common.model.MediaType
import tv.trakt.trakt.common.model.TraktId

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

    @Immutable
    internal data class Person(
        override val rank: Long,
        val person: tv.trakt.trakt.common.model.Person,
        val showBirthday: Boolean = false,
    ) : SearchItem(rank)

    val id: TraktId
        get() = when (this) {
            is Movie -> movie.ids.trakt
            is Show -> show.ids.trakt
            is Person -> person.ids.trakt
        }

    val type: MediaType?
        get() = when (this) {
            is Movie -> MediaType.MOVIE
            is Show -> MediaType.SHOW
            else -> null
        }

    val key: String
        get() = when (this) {
            is Movie -> "movie_${movie.ids.trakt.value}"
            is Show -> "show_${show.ids.trakt.value}"
            is Person -> "person_${person.ids.trakt.value}"
        }

    val images: Images?
        get() = when (this) {
            is Movie -> movie.images
            is Show -> show.images
            is Person -> person.images
        }
}
