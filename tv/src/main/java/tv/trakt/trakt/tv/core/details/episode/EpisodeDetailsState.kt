package tv.trakt.trakt.tv.core.details.episode

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import tv.trakt.trakt.common.model.Show
import tv.trakt.trakt.common.model.SlugId
import tv.trakt.trakt.common.model.User
import tv.trakt.trakt.tv.common.model.CastPerson
import tv.trakt.trakt.tv.common.model.Comment
import tv.trakt.trakt.tv.common.model.ExternalRating
import tv.trakt.trakt.tv.common.model.StreamingService
import tv.trakt.trakt.tv.common.model.SyncHistoryEpisodeItem
import tv.trakt.trakt.tv.core.episodes.model.Episode
import tv.trakt.trakt.tv.helpers.StringResource

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
