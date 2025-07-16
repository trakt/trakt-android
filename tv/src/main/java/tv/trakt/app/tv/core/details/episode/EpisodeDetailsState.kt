package tv.trakt.app.tv.core.details.episode

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import tv.trakt.app.tv.common.model.CastPerson
import tv.trakt.app.tv.common.model.Comment
import tv.trakt.app.tv.common.model.ExternalRating
import tv.trakt.app.tv.common.model.SlugId
import tv.trakt.app.tv.common.model.StreamingService
import tv.trakt.app.tv.common.model.SyncHistoryEpisodeItem
import tv.trakt.app.tv.common.model.User
import tv.trakt.app.tv.core.episodes.model.Episode
import tv.trakt.app.tv.core.shows.model.Show
import tv.trakt.app.tv.helpers.StringResource

@Immutable
internal data class EpisodeDetailsState(
    val user: User? = null,
    val showDetails: Show? = null,
    val episodeDetails: Episode? = null,
    val episodeRatings: ExternalRating? = null,
    val episodeCast: ImmutableList<CastPerson>? = null,
    val episodeStreamings: StreamingsState = StreamingsState(),
    val episodeComments: ImmutableList<Comment>? = null,
    val episodeRelated: ImmutableList<Show>? = null,
    val episodeSeason: ImmutableList<Episode>? = null,
    val episodeHistory: HistoryState = HistoryState(),
    val isLoading: Boolean = false,
    val snackMessage: StringResource? = null,
) {
    @Immutable
    internal data class StreamingsState(
        val slug: SlugId? = null,
        val service: StreamingService? = null,
        val isLoading: Boolean = false,
    )

    @Immutable
    internal data class HistoryState(
        val isLoading: Boolean = false,
        val episodes: ImmutableList<SyncHistoryEpisodeItem>? = null,
    ) {
        val episodesPlays: Int
            get() = episodes?.size ?: 0
    }
}
