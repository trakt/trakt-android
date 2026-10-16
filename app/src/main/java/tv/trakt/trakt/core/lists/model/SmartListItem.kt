package tv.trakt.trakt.core.lists.model

import androidx.compose.runtime.Immutable
import timber.log.Timber
import tv.trakt.trakt.common.model.Images
import tv.trakt.trakt.common.model.MediaType
import tv.trakt.trakt.common.model.Movie
import tv.trakt.trakt.common.model.Show
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.model.fromDto
import tv.trakt.trakt.common.networking.SmartListItemDto

@Immutable
internal sealed interface SmartListItem {
    @Immutable
    data class MovieItem(
        val movie: Movie,
    ) : SmartListItem

    @Immutable
    data class ShowItem(
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

    companion object {
        fun fromDto(dto: SmartListItemDto): SmartListItem? {
            val show = dto.show
            val movie = dto.movie
            return when {
                show != null -> {
                    ShowItem(show = Show.fromDto(show))
                }
                movie != null -> {
                    MovieItem(movie = Movie.fromDto(movie))
                }
                else -> {
                    Timber.w("Smart list item missing media: type=%s", dto.type)
                    null
                }
            }
        }
    }
}
