package tv.trakt.trakt.core.summary.shows.features.seasons.all

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType.Companion.Confirm
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.launch
import tv.trakt.trakt.LocalSnackbarState
import tv.trakt.trakt.common.model.Episode
import tv.trakt.trakt.common.model.Person
import tv.trakt.trakt.common.model.Show
import tv.trakt.trakt.common.model.TraktId

@Composable
internal fun AllShowSeasonsScreen(
    viewModel: AllShowSeasonsViewModel,
    onEpisodeClick: (showId: TraktId, episode: Episode) -> Unit,
    onPersonClick: (show: Show, person: Person) -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val snack = LocalSnackbarState.current
    val scope = rememberCoroutineScope()

    val state by viewModel.state.collectAsStateWithLifecycle()
    val sheetState = rememberAllShowSeasonsSheetState()

    AllShowSeasonsContent(
        state = state,
        modifier = modifier,
        onModeClick = viewModel::setMode,
        onPeopleModeClick = viewModel::setPeopleMode,
        onCommentsFilterClick = viewModel::setCommentsFilter,
        onCommentRepliesClick = {
            viewModel.loadSeasonCommentReplies(it.id)
        },
        onCommentReactionsRequest = {
            viewModel.loadSeasonCommentReactions(it.id)
        },
        onCommentReactionClick = { reaction, comment ->
            viewModel.setSeasonCommentReaction(reaction, comment.id)
        },
        onNewCommentClick = { sheetState.postComment = true },
        onCommentReplyClick = { sheetState.postReply = it to null },
        onCommentReplyUserClick = { comment, user -> sheetState.postReply = comment to user },
        onCommentDeleteClick = { sheetState.deleteComment = it },
        onCommentReplyDeleteClick = { sheetState.deleteReply = it },
        onPersonClick = { person -> state.show?.let { onPersonClick(it, person) } },
        onSeasonRatingClick = {
            viewModel.addSeasonRating(it)
            haptic.performHapticFeedback(Confirm)
        },
        onSeasonRatingRemoveClick = {
            viewModel.removeSeasonRating()
            haptic.performHapticFeedback(Confirm)
        },
        onEpisodeClick = { viewModel.navigateToEpisode(it.episode) },
        onSeasonClick = {
            viewModel.loadSeason(it)
            viewModel.loadSeasonPeople(it.season, clear = true)
            viewModel.loadSeasonComments(it.season, clear = true)
        },
        onCheckEpisodeClick = { viewModel.addToWatched(it.episode) },
        onCheckEpisodeLongClick = { sheetState.episodeDate = it },
        onMoreClick = { sheetState.episodeContext = it },
        onCheckSeasonClick = { sheetState.confirmMarkSeason = true },
        onSeasonMoreClick = { sheetState.seasonContext = it },
        onBackClick = onNavigateBack,
    )

    AllShowSeasonsSheets(
        state = state,
        sheetState = sheetState,
        viewModel = viewModel,
    )

    LaunchedEffect(state.info) {
        if (state.info == null) return@LaunchedEffect
        scope.launch {
            haptic.performHapticFeedback(Confirm)
            state.info?.get(context)?.let { snack.showSnackbar(it) }
        }
        viewModel.clearInfo()
    }

    LaunchedEffect(state.navigateEpisode) {
        state.navigateEpisode?.let {
            onEpisodeClick(it.first, it.second)
            viewModel.clearNavigation()
        }
    }
}
