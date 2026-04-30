package tv.trakt.trakt.core.search.model

import androidx.compose.runtime.Immutable
import tv.trakt.trakt.common.model.CustomList
import tv.trakt.trakt.common.model.Images
import tv.trakt.trakt.common.model.MediaType
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.model.Movie as MovieModel
import tv.trakt.trakt.common.model.Person as PersonModel
import tv.trakt.trakt.common.model.Show as ShowModel

@Immutable
internal sealed class SearchItem(
    open val rank: Long,
) {
    @Immutable
    internal data class Movie(
        override val rank: Long,
        val movie: MovieModel,
    ) : SearchItem(rank)

    @Immutable
    internal data class Show(
        override val rank: Long,
        val show: ShowModel,
    ) : SearchItem(rank)

    @Immutable
    internal data class Person(
        override val rank: Long,
        val person: PersonModel,
        val showBirthday: Boolean = false,
    ) : SearchItem(rank)

    @Immutable
    internal data class List(
        val list: CustomList,
        val liked: Boolean,
    ) : SearchItem(rank = 0L)

    val id: TraktId
        get() = when (this) {
            is Movie -> movie.ids.trakt
            is Show -> show.ids.trakt
            is Person -> person.ids.trakt
            is List -> list.ids.trakt
        }

    val type: MediaType?
        get() = when (this) {
            is Movie -> MediaType.MOVIE
            is Show -> MediaType.SHOW
            is Person -> null
            is List -> null
        }

    val key: String
        get() = when (this) {
            is Movie -> "movie-${movie.ids.trakt.value}"
            is Show -> "show-${show.ids.trakt.value}"
            is Person -> "person-${person.ids.trakt.value}"
            is List -> "list-${list.ids.trakt.value}"
        }

    val images: Images?
        get() = when (this) {
            is Movie -> movie.images
            is Show -> show.images
            is Person -> person.images
            is List -> list.images
        }

    val airedEpisodes: Int?
        get() = when (this) {
            is Show -> show.airedEpisodes
            else -> null
        }
}
