package tv.trakt.trakt.app.core.lists.details.personal.model

import androidx.compose.runtime.Immutable
import tv.trakt.trakt.common.model.Images
import tv.trakt.trakt.common.model.Movie
import tv.trakt.trakt.common.model.Show

@Immutable
internal data class PersonalListItem(
    val rank: Int,
    val type: String,
    val show: Show? = null,
    val movie: Movie? = null,
) {
    val id: String
        get() {
            return "${(show?.ids?.trakt ?: movie?.ids?.trakt)}-$type"
        }

    val images: Images?
        get() {
            return show?.images ?: movie?.images
        }
}
