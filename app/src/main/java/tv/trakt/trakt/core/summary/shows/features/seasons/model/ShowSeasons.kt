package tv.trakt.trakt.core.summary.shows.features.seasons.model

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.ImmutableSet
import kotlinx.collections.immutable.persistentMapOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.collections.immutable.toImmutableMap
import tv.trakt.trakt.common.core.sync.model.ProgressItem.ShowItem
import tv.trakt.trakt.common.helpers.extensions.EmptyImmutableList
import tv.trakt.trakt.common.helpers.extensions.EmptyImmutableSet
import tv.trakt.trakt.common.model.CastPerson
import tv.trakt.trakt.common.model.Comment
import tv.trakt.trakt.common.model.CrewPerson
import tv.trakt.trakt.common.model.Episode
import tv.trakt.trakt.common.model.Season
import tv.trakt.trakt.common.model.TraktId

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

    /**
     * Released episodes still unwatched between the show's first episode and
     * [episode]: the gaps a "watched until here" would fill. Specials are left
     * out, matching what GetWatchedUntilEpisodesUseCase would actually mark.
     */
    fun skippedEpisodesBefore(episode: Episode): Int {
        if (episode.season <= 0) return 0

        val previousSeasons = seasons
            .filter { it.season.number in 1 until episode.season }
            .sumOf { it.unwatchedEpisodes.coerceAtLeast(0) }

        val sameSeason = selectedSeasonEpisodes.count {
            it.episode.season == episode.season &&
                it.episode.number < episode.number &&
                it.episode.isReleased &&
                !it.isWatched
        }

        return previousSeasons + sameSeason
    }

    /** Prepends a freshly posted [comment] to the selected season's comment list. */
    fun addComment(comment: Comment): ShowSeasons =
        copy(
            selectedSeasonComments = (listOf(comment) + selectedSeasonComments).toImmutableList(),
        )

    /** Removes the comment identified by [commentId] from the selected season's comment list. */
    fun deleteComment(commentId: TraktId): ShowSeasons =
        copy(
            selectedSeasonComments = selectedSeasonComments
                .filterNot { it.id == commentId.value }
                .toImmutableList(),
        )

    /** Appends [reply] under its parent comment and bumps that comment's reply count. */
    fun addReply(reply: Comment): ShowSeasons {
        val replies = selectedSeasonReplies.toMutableMap()
        val current = replies[reply.parentId]?.toMutableList() ?: mutableListOf()
        current.add(reply)
        replies[reply.parentId] = current.toImmutableList()

        val comments = selectedSeasonComments.map { comment ->
            if (comment.id == reply.parentId) {
                comment.copy(replies = comment.replies + 1)
            } else {
                comment
            }
        }.toImmutableList()

        return copy(
            selectedSeasonReplies = replies.toImmutableMap(),
            selectedSeasonComments = comments,
        )
    }

    /** Removes reply [replyId] under [parentId] and decrements that comment's reply count. */
    fun deleteReply(
        parentId: TraktId,
        replyId: TraktId,
    ): ShowSeasons {
        val replies = selectedSeasonReplies.toMutableMap()
        replies[parentId.value]?.let { current ->
            replies[parentId.value] = current
                .filterNot { reply -> reply.id == replyId.value }
                .toImmutableList()
        }

        val comments = selectedSeasonComments.map { comment ->
            if (comment.id == parentId.value) {
                comment.copy(replies = (comment.replies - 1).coerceAtLeast(0))
            } else {
                comment
            }
        }.toImmutableList()

        return copy(
            selectedSeasonReplies = replies.toImmutableMap(),
            selectedSeasonComments = comments,
        )
    }

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
