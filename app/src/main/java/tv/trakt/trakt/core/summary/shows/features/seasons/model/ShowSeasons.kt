package tv.trakt.trakt.core.summary.shows.features.seasons.model

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.ImmutableSet
import kotlinx.collections.immutable.persistentMapOf
import kotlinx.collections.immutable.toImmutableList
import tv.trakt.trakt.common.core.sync.model.ProgressItem.ShowItem
import tv.trakt.trakt.common.helpers.extensions.EmptyImmutableList
import tv.trakt.trakt.common.helpers.extensions.EmptyImmutableSet
import tv.trakt.trakt.common.model.CastPerson
import tv.trakt.trakt.common.model.Comment
import tv.trakt.trakt.common.model.CrewPerson
import tv.trakt.trakt.common.model.Season

@Immutable
internal data class ShowSeasons(
    val seasons: ImmutableList<SeasonItem> = EmptyImmutableList,
    val selectedSeason: Season? = null,
    val selectedSeasonEpisodes: ImmutableList<EpisodeItem> = EmptyImmutableList,
    val selectedSeasonCast: ImmutableList<CastPerson> = EmptyImmutableList,
    val selectedSeasonCrew: ImmutableList<CrewPerson> = EmptyImmutableList,
    val selectedSeasonComments: ImmutableList<Comment> = EmptyImmutableList,
    val selectedSeasonReplies: ImmutableMap<Int, ImmutableList<Comment>> = persistentMapOf(),
    val selectedSeasonRepliesLoading: ImmutableSet<Int> = EmptyImmutableSet,
    val isSeasonLoading: Boolean = false,
    val isSeasonPeopleLoading: Boolean = false,
    val isSeasonCommentsLoading: Boolean = false,
) {
    val isSelectedSeasonWatched: Boolean
        get() = selectedSeasonEpisodes.isNotEmpty() &&
            selectedSeasonEpisodes.all { it.isWatched }

    companion object Helpers {
        fun markWatchedEpisodes(
            inputEpisodes: List<EpisodeItem>,
            progress: ImmutableList<ShowItem.Season>?,
            checkable: Boolean,
        ): ImmutableList<EpisodeItem> {
            val watchedBySeasonNumber = progress?.associate { season ->
                season.number to season.episodes.mapTo(hashSetOf()) { it.id }
            }
            return inputEpisodes
                .map {
                    it.copy(
                        isLoading = false,
                        isCheckable = checkable,
                        isWatched = watchedBySeasonNumber
                            ?.get(it.episode.season)
                            ?.contains(it.episode.ids.trakt) == true,
                    )
                }.toImmutableList()
        }

        fun markWatchedSeasons(
            inputSeasons: List<SeasonItem>,
            progress: ImmutableList<ShowItem.Season>?,
        ): ImmutableList<SeasonItem> {
            val progressMap = progress?.associateBy { it.number }
            return inputSeasons
                .map {
                    val watchedCount = progressMap
                        ?.get(it.season.number)
                        ?.episodes
                        ?.size
                        ?: 0

                    it.copy(
                        isWatched = watchedCount == it.season.episodeCount,
                        isWatching = watchedCount in 1 until (it.season.episodeCount ?: 0),
                        watchedEpisodes = watchedCount,
                        unwatchedEpisodes = (it.season.episodeCount ?: 0) - watchedCount,
                    )
                }.toImmutableList()
        }
    }
}
