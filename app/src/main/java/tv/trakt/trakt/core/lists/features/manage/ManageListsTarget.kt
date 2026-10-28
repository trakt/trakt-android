package tv.trakt.trakt.core.lists.features.manage

import androidx.compose.runtime.Immutable
import tv.trakt.trakt.common.model.Episode
import tv.trakt.trakt.common.model.Season
import tv.trakt.trakt.common.model.Show
import tv.trakt.trakt.common.model.TraktId

@Immutable
internal sealed interface ManageListsTarget {
    val id: TraktId
    val show: Show

    @Immutable
    data class SeasonTarget(
        override val show: Show,
        val season: Season,
    ) : ManageListsTarget {
        override val id: TraktId
            get() = season.ids.trakt
    }

    @Immutable
    data class EpisodeTarget(
        override val show: Show,
        val episode: Episode,
    ) : ManageListsTarget {
        override val id: TraktId
            get() = episode.ids.trakt
    }
}
