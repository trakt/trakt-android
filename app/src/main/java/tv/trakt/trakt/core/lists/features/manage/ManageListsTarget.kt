package tv.trakt.trakt.core.lists.features.manage

import androidx.compose.runtime.Immutable
import tv.trakt.trakt.common.model.Episode
import tv.trakt.trakt.common.model.Movie
import tv.trakt.trakt.common.model.Season
import tv.trakt.trakt.common.model.Show
import tv.trakt.trakt.common.model.TraktId

@Immutable
internal sealed interface ManageListsTarget {
    val id: TraktId

    @Immutable
    data class MovieTarget(
        val movie: Movie,
    ) : ManageListsTarget {
        override val id: TraktId
            get() = movie.ids.trakt
    }

    @Immutable
    data class ShowTarget(
        val show: Show,
    ) : ManageListsTarget {
        override val id: TraktId
            get() = show.ids.trakt
    }

    @Immutable
    data class SeasonTarget(
        val show: Show,
        val season: Season,
    ) : ManageListsTarget {
        override val id: TraktId
            get() = season.ids.trakt
    }

    @Immutable
    data class EpisodeTarget(
        val show: Show,
        val episode: Episode,
    ) : ManageListsTarget {
        override val id: TraktId
            get() = episode.ids.trakt
    }
}
