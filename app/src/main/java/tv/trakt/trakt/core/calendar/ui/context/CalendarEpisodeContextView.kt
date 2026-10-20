@file:OptIn(ExperimentalMaterial3Api::class)

package tv.trakt.trakt.core.calendar.ui.context

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow.Companion.Ellipsis
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.collections.immutable.persistentListOf
import tv.trakt.trakt.common.helpers.LoadingState
import tv.trakt.trakt.common.helpers.preview.PreviewData
import tv.trakt.trakt.common.model.calendar.CalendarItem
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.components.buttons.GhostButton
import tv.trakt.trakt.ui.components.confirmation.ConfirmationSheet
import tv.trakt.trakt.ui.components.confirmation.RemoveConfirmationSheet
import tv.trakt.trakt.ui.components.dateselection.DateSelectionSheet
import tv.trakt.trakt.ui.theme.TraktTheme

@Composable
internal fun CalendarEpisodeContextView(
    item: CalendarItem.EpisodeItem,
    viewModel: CalendarEpisodeContextViewModel,
    modifier: Modifier = Modifier,
    showWatchlist: Boolean,
    showDrop: Boolean,
    onAddWatched: () -> Unit,
    onRemoveWatched: () -> Unit,
    onAddWatchlist: () -> Unit,
    onRemoveWatchlist: () -> Unit,
    onDropped: () -> Unit,
    onError: () -> Unit,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val subtitle = item.subtitle()

    var confirmRemoveWatchedSheet by remember { mutableStateOf(false) }
    var confirmRemoveWatchlistSheet by remember { mutableStateOf(false) }
    var confirmDropSheet by remember { mutableStateOf(false) }
    var dateSheet by remember { mutableStateOf(false) }

    LaunchedEffect(state.loadingWatched, state.loadingWatchlist, state.loadingDrop) {
        when {
            state.loadingWatched == LoadingState.Done -> when {
                !state.isWatched -> onAddWatched()
                else -> onRemoveWatched()
            }

            state.loadingWatchlist == LoadingState.Done -> when {
                !state.isWatchlist -> onAddWatchlist()
                else -> onRemoveWatchlist()
            }

            state.loadingDrop == LoadingState.Done -> onDropped()
        }
    }

    LaunchedEffect(state.error) {
        if (state.error != null) {
            onError()
        }
    }

    CalendarEpisodeContextViewContent(
        item = item,
        subtitle = subtitle,
        state = state,
        showWatchlist = showWatchlist,
        showDrop = showDrop,
        modifier = modifier,
        onWatchedClick = {
            when {
                state.isWatched -> confirmRemoveWatchedSheet = true
                else -> dateSheet = true
            }
        },
        onWatchlistClick = {
            when {
                state.isWatchlist -> confirmRemoveWatchlistSheet = true
                else -> viewModel.addToWatchlist()
            }
        },
        onDropClick = {
            confirmDropSheet = true
        },
    )

    RemoveConfirmationSheet(
        active = confirmRemoveWatchedSheet,
        onYes = {
            confirmRemoveWatchedSheet = false
            viewModel.removeFromWatched()
        },
        onNo = { confirmRemoveWatchedSheet = false },
        title = stringResource(R.string.button_text_remove_from_history),
        message = stringResource(
            R.string.warning_prompt_remove_from_watched,
            subtitle,
        ),
    )

    RemoveConfirmationSheet(
        active = confirmRemoveWatchlistSheet,
        onYes = {
            confirmRemoveWatchlistSheet = false
            viewModel.removeFromWatchlist()
        },
        onNo = { confirmRemoveWatchlistSheet = false },
        title = stringResource(R.string.button_text_watchlist),
        message = stringResource(
            R.string.warning_prompt_remove_from_watchlist,
            item.show.title,
        ),
    )

    ConfirmationSheet(
        active = confirmDropSheet,
        onYes = {
            confirmDropSheet = false
            viewModel.dropShow()
        },
        onNo = { confirmDropSheet = false },
        title = stringResource(R.string.button_text_drop_show),
        message = stringResource(
            R.string.warning_prompt_drop_show,
            item.show.title,
        ),
    )

    DateSelectionSheet(
        active = dateSheet,
        title = item.show.title,
        subtitle = subtitle,
        onResult = viewModel::addToWatched,
        onDismiss = {
            dateSheet = false
        },
    )
}

@Composable
private fun CalendarEpisodeContextViewContent(
    item: CalendarItem.EpisodeItem,
    subtitle: String,
    state: CalendarEpisodeContextState,
    showWatchlist: Boolean,
    showDrop: Boolean,
    modifier: Modifier = Modifier,
    onWatchedClick: () -> Unit = {},
    onWatchlistClick: () -> Unit = {},
    onDropClick: () -> Unit = {},
) {
    Column(
        verticalArrangement = spacedBy(0.dp),
        modifier = modifier,
    ) {
        Column(
            verticalArrangement = spacedBy(2.dp),
        ) {
            Text(
                text = item.show.title,
                color = TraktTheme.colors.textPrimary,
                style = TraktTheme.typography.heading3,
                maxLines = 1,
                overflow = Ellipsis,
                autoSize = TextAutoSize.StepBased(
                    maxFontSize = TraktTheme.typography.heading3.fontSize,
                    minFontSize = 20.sp,
                    stepSize = 2.sp,
                ),
            )

            Text(
                text = subtitle,
                color = TraktTheme.colors.textSecondary,
                style = TraktTheme.typography.paragraphSmall,
                maxLines = 1,
                overflow = Ellipsis,
            )
        }

        Spacer(
            modifier = Modifier
                .padding(top = 22.dp)
                .background(TraktTheme.colors.separator)
                .fillMaxWidth()
                .height(1.dp),
        )

        if (state.user != null) {
            CalendarEpisodeActionButtons(
                item = item,
                state = state,
                showWatchlist = showWatchlist,
                showDrop = showDrop,
                onWatchedClick = onWatchedClick,
                onWatchlistClick = onWatchlistClick,
                onDropClick = onDropClick,
                modifier = Modifier.padding(top = 14.dp),
            )
        }
    }
}

@Composable
private fun CalendarEpisodeActionButtons(
    item: CalendarItem.EpisodeItem,
    state: CalendarEpisodeContextState,
    showWatchlist: Boolean,
    showDrop: Boolean,
    modifier: Modifier = Modifier,
    onWatchedClick: () -> Unit,
    onWatchlistClick: () -> Unit,
    onDropClick: () -> Unit,
) {
    val isReleased = item.episodes.last().rememberReleased()

    val isLoadingOrDone =
        state.loadingWatched.isLoading ||
            state.loadingWatchlist.isLoading ||
            state.loadingDrop.isLoading ||
            state.loadingWatched.isDone ||
            state.loadingWatchlist.isDone ||
            state.loadingDrop.isDone

    Column(
        verticalArrangement = spacedBy(TraktTheme.spacing.contextItemsSpace),
        modifier = modifier,
    ) {
        if (isReleased) {
            GhostButton(
                enabled = !isLoadingOrDone,
                loading = state.loadingWatched.isLoading || state.loadingWatched.isDone,
                text = when {
                    state.isWatched -> stringResource(R.string.button_text_remove_from_history)
                    else -> stringResource(R.string.button_text_track)
                },
                onClick = onWatchedClick,
                icon = when {
                    state.isWatched -> painterResource(R.drawable.ic_trash)
                    else -> painterResource(R.drawable.ic_check_2)
                },
                iconSize = 22.dp,
                iconSpace = 16.dp,
                modifier = Modifier
                    .graphicsLayer {
                        translationX = -6.dp.toPx()
                    },
            )
        }

        if (showWatchlist) {
            GhostButton(
                enabled = !isLoadingOrDone,
                loading = state.loadingWatchlist.isLoading || state.loadingWatchlist.isDone,
                text = when {
                    state.isWatchlist -> stringResource(R.string.button_text_remove_from_watchlist)
                    else -> stringResource(R.string.button_text_watchlist)
                },
                onClick = onWatchlistClick,
                icon = when {
                    state.isWatchlist -> painterResource(R.drawable.ic_bookmark_on)
                    else -> painterResource(R.drawable.ic_bookmark_off)
                },
                iconSize = 25.dp,
                iconSpace = 16.dp,
                modifier = Modifier
                    .graphicsLayer {
                        translationX = -8.dp.toPx()
                    },
            )
        }

        if (showDrop) {
            GhostButton(
                enabled = !isLoadingOrDone,
                loading = state.loadingDrop.isLoading || state.loadingDrop.isDone,
                text = stringResource(R.string.button_text_drop_show),
                onClick = onDropClick,
                icon = painterResource(R.drawable.ic_drop),
                iconSize = 21.dp,
                iconSpace = 16.dp,
                modifier = Modifier
                    .graphicsLayer {
                        translationX = -8.dp.toPx()
                    },
            )
        }
    }
}

@Composable
private fun CalendarItem.EpisodeItem.subtitle(): String {
    return when {
        isFullSeason -> stringResource(
            R.string.text_season_number,
            episode.season,
        )

        episodes.size > 1 -> stringResource(
            R.string.episode_footer_season_episode_range,
            episode.season,
            episodes.first().number,
            episodes.last().number,
        )

        else -> episode.seasonEpisodeString()
    }
}

@Preview(
    device = "id:pixel_5",
    showBackground = true,
    backgroundColor = 0xFF212427,
)
@Composable
private fun Preview() {
    TraktTheme {
        val item = CalendarItem.EpisodeItem(
            show = PreviewData.show1,
            episodes = persistentListOf(PreviewData.episode1),
            watched = false,
        )
        CalendarEpisodeContextViewContent(
            item = item,
            subtitle = item.subtitle(),
            state = CalendarEpisodeContextState(
                user = PreviewData.user1,
            ),
            showWatchlist = true,
            showDrop = false,
        )
    }
}

@Preview(
    device = "id:pixel_5",
    showBackground = true,
    backgroundColor = 0xFF212427,
)
@Composable
private fun PreviewWatched() {
    TraktTheme {
        val item = CalendarItem.EpisodeItem(
            show = PreviewData.show1,
            episodes = persistentListOf(PreviewData.episode1),
            watched = true,
        )
        CalendarEpisodeContextViewContent(
            item = item,
            subtitle = item.subtitle(),
            state = CalendarEpisodeContextState(
                isWatched = true,
                isWatchlist = true,
                user = PreviewData.user1,
            ),
            showWatchlist = false,
            showDrop = true,
        )
    }
}
