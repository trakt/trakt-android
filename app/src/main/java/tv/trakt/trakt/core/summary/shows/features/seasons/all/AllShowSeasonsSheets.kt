@file:OptIn(ExperimentalMaterial3Api::class)

package tv.trakt.trakt.core.summary.shows.features.seasons.all

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import tv.trakt.trakt.common.model.Comment
import tv.trakt.trakt.common.model.MediaType
import tv.trakt.trakt.common.model.User
import tv.trakt.trakt.common.model.toTraktId
import tv.trakt.trakt.core.comments.features.deletecomment.DeleteCommentSheet
import tv.trakt.trakt.core.comments.features.postcomment.PostCommentSheet
import tv.trakt.trakt.core.comments.features.postreply.PostReplySheet
import tv.trakt.trakt.core.summary.shows.features.context.episodes.EpisodeContextSheet
import tv.trakt.trakt.core.summary.shows.features.seasons.model.EpisodeItem
import tv.trakt.trakt.core.summary.shows.features.seasons.watcheduntil.WatchedUntilSheet
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.components.confirmation.ConfirmationSheet
import tv.trakt.trakt.ui.components.confirmation.RemoveConfirmationSheet
import tv.trakt.trakt.ui.components.dateselection.DateSelectionSheet

/**
 * Holds the transient visibility state for every bottom sheet shown on the
 * all-seasons screen. Owned by [AllShowSeasonsScreen] and mutated both by the
 * content callbacks and by the sheets themselves.
 */
@Stable
internal class AllShowSeasonsSheetState {
    var confirmRemoveEpisode by mutableStateOf<EpisodeItem?>(null)
    var confirmMarkSeason by mutableStateOf(false)
    var confirmRemoveSeason by mutableStateOf(false)
    var episodeDate by mutableStateOf<EpisodeItem?>(null)
    var episodeWatchedUntil by mutableStateOf<EpisodeItem?>(null)
    var episodeContext by mutableStateOf<EpisodeItem?>(null)
    var seasonDate by mutableStateOf(false)
    var postComment by mutableStateOf(false)
    var postReply by mutableStateOf<Pair<Comment, User?>?>(null)
    var deleteComment by mutableStateOf<Comment?>(null)
    var deleteReply by mutableStateOf<Comment?>(null)
}

@Composable
internal fun rememberAllShowSeasonsSheetState(): AllShowSeasonsSheetState =
    remember { AllShowSeasonsSheetState() }

@Composable
internal fun AllShowSeasonsSheets(
    state: AllShowSeasonsState,
    sheetState: AllShowSeasonsSheetState,
    viewModel: AllShowSeasonsViewModel,
) {
    EpisodeContextSheet(
        episodeItem = sheetState.episodeContext,
        onTrackClick = {
            sheetState.episodeDate = it
        },
        onWatchedUntilClick = {
            sheetState.episodeWatchedUntil = it
        },
        onRemoveClick = {
            sheetState.confirmRemoveEpisode = it
        },
        onDismiss = {
            sheetState.episodeContext = null
        },
    )

    WatchedUntilSheet(
        show = state.show,
        episode = sheetState.episodeWatchedUntil?.episode,
        onDismiss = {
            sheetState.episodeWatchedUntil = null
        },
    )

    RemoveConfirmationSheet(
        active = sheetState.confirmRemoveEpisode != null,
        onYes = {
            sheetState.confirmRemoveEpisode?.let {
                viewModel.removeFromWatched(it.episode)
                sheetState.confirmRemoveEpisode = null
            }
        },
        onNo = { sheetState.confirmRemoveEpisode = null },
        title = stringResource(R.string.button_text_remove_from_history),
        message = stringResource(
            R.string.warning_prompt_remove_from_watched,
            "${sheetState.confirmRemoveEpisode?.episode?.title}",
        ),
    )

    ConfirmationSheet(
        active = sheetState.confirmMarkSeason,
        onYes = {
            sheetState.confirmMarkSeason = false
            sheetState.seasonDate = true
        },
        onNo = { sheetState.confirmMarkSeason = false },
        title = stringResource(R.string.button_text_track),
        message = stringResource(
            R.string.warning_prompt_mark_as_watched_multiple_episodes,
            state.items.selectedSeasonEpisodes.count { !it.isWatched && it.episode.isReleased },
        ),
    )

    RemoveConfirmationSheet(
        active = sheetState.confirmRemoveSeason,
        onYes = {
            sheetState.confirmRemoveSeason = false
            state.items.selectedSeason?.let { viewModel.removeFromWatched(it) }
        },
        onNo = { sheetState.confirmRemoveSeason = false },
        title = stringResource(R.string.button_text_remove_from_history),
        message = stringResource(
            R.string.warning_prompt_remove_from_watched,
            stringResource(
                R.string.text_season_number,
                state.items.selectedSeason?.number ?: 0,
            ),
        ),
    )

    DateSelectionSheet(
        active = sheetState.episodeDate != null,
        title = state.show?.title ?: "",
        onResult = { result ->
            sheetState.episodeDate?.let {
                viewModel.addToWatched(
                    episode = it.episode,
                    customDate = result,
                )
            }
        },
        onDismiss = { sheetState.episodeDate = null },
    )

    DateSelectionSheet(
        active = sheetState.seasonDate,
        title = state.show?.title ?: "",
        onResult = { result ->
            viewModel.addToWatched(season = state.items, customDate = result)
        },
        onDismiss = { sheetState.seasonDate = false },
    )

    PostCommentSheet(
        active = sheetState.postComment,
        mediaId = state.items.selectedSeason?.ids?.trakt,
        mediaType = MediaType.Season,
        onCommentPost = viewModel::addSeasonComment,
        onDismiss = { sheetState.postComment = false },
    )

    PostReplySheet(
        active = sheetState.postReply != null,
        comment = sheetState.postReply?.first,
        user = sheetState.postReply?.second,
        onReplyPost = viewModel::addSeasonReply,
        onDismiss = { sheetState.postReply = null },
    )

    DeleteCommentSheet(
        active = sheetState.deleteComment != null,
        commentId = sheetState.deleteComment?.id?.toTraktId(),
        onDeleted = viewModel::deleteSeasonComment,
        onDismiss = { sheetState.deleteComment = null },
    )

    DeleteCommentSheet(
        isReply = true,
        active = sheetState.deleteReply != null,
        commentId = sheetState.deleteReply?.id?.toTraktId(),
        onDeleted = {
            sheetState.deleteReply?.let {
                viewModel.deleteSeasonReply(
                    parentId = it.parentId.toTraktId(),
                    replyId = it.id.toTraktId(),
                )
            }
        },
        onDismiss = { sheetState.deleteReply = null },
    )
}
