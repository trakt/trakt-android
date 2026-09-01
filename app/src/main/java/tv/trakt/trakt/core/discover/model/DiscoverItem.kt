package tv.trakt.trakt.core.discover.model

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.serialization.Serializable
import tv.trakt.trakt.common.helpers.serializers.ImmutableListSerializer
import tv.trakt.trakt.common.model.Images
import tv.trakt.trakt.common.model.MediaType
import tv.trakt.trakt.common.model.Movie
import tv.trakt.trakt.common.model.Show
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.core.home.sections.recommended.model.RecommendedSource
import tv.trakt.trakt.resources.R

@Immutable
@Serializable
internal sealed class DiscoverItem {
    abstract val count: Int
    abstract val sources: ImmutableList<RecommendedSource>

    @Immutable
    @Serializable
    internal data class MovieItem(
        val movie: Movie,
        override val count: Int = 0,
        @Serializable(ImmutableListSerializer::class)
        override val sources: ImmutableList<RecommendedSource> = persistentListOf(),
    ) : DiscoverItem()

    @Immutable
    @Serializable
    internal data class ShowItem(
        val show: Show,
        override val count: Int = 0,
        @Serializable(ImmutableListSerializer::class)
        override val sources: ImmutableList<RecommendedSource> = persistentListOf(),
    ) : DiscoverItem()

    val type: MediaType
        get() = when (this) {
            is ShowItem -> MediaType.Show
            is MovieItem -> MediaType.Movie
        }

    val id: TraktId
        get() = when (this) {
            is ShowItem -> show.ids.trakt
            is MovieItem -> movie.ids.trakt
        }

    val key: String
        get() = when (this) {
            is ShowItem -> "${id.value}-show"
            is MovieItem -> "${id.value}-movie"
        }

    val title: String
        get() = when (this) {
            is ShowItem -> show.title
            is MovieItem -> movie.title
        }

    val icon: Int
        get() = when (this) {
            is ShowItem -> R.drawable.ic_shows_off
            is MovieItem -> R.drawable.ic_movies_off
        }

    val images: Images?
        get() = when (this) {
            is ShowItem -> show.images
            is MovieItem -> movie.images
        }

    val airedEpisodes: Int?
        get() = when (this) {
            is ShowItem -> show.airedEpisodes
            is MovieItem -> null
        }
}
