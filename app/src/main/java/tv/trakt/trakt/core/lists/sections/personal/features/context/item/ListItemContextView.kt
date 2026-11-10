@file:OptIn(ExperimentalMaterial3Api::class)

package tv.trakt.trakt.core.lists.sections.personal.features.context.item

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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import tv.trakt.trakt.common.helpers.LoadingState
import tv.trakt.trakt.common.helpers.extensions.PreviewTraktLightDark
import tv.trakt.trakt.common.helpers.preview.PreviewData
import tv.trakt.trakt.common.model.lists.CustomList
import tv.trakt.trakt.core.lists.model.CustomListItem
import tv.trakt.trakt.core.lists.model.CustomListItem.EpisodeItem
import tv.trakt.trakt.core.lists.model.CustomListItem.MovieItem
import tv.trakt.trakt.core.lists.model.CustomListItem.SeasonItem
import tv.trakt.trakt.core.lists.model.CustomListItem.ShowItem
import tv.trakt.trakt.helpers.extensions.TraktThemePreview
import tv.trakt.trakt.helpers.extensions.isSpoilerHidden
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.components.buttons.GhostButton
import tv.trakt.trakt.ui.components.confirmation.RemoveConfirmationSheet
import tv.trakt.trakt.ui.components.dateselection.DateSelectionSheet
import tv.trakt.trakt.ui.theme.TraktTheme
import java.time.Instant

@Composable
internal fun ListItemContextView(
    item: CustomListItem,
    list: CustomList,
    removable: Boolean,
    viewModel: ListItemContextViewModel,
    modifier: Modifier = Modifier,
    onAddWatched: () -> Unit,
    onRemoveWatched: () -> Unit,
    onRemoveList: () -> Unit,
    onListsClick: () -> Unit,
    onError: () -> Unit,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    var confirmRemoveWatchedSheet by remember { mutableStateOf(false) }
    var confirmRemoveListSheet by remember { mutableStateOf(false) }
    var dateSheet by remember { mutableStateOf(false) }

    LaunchedEffect(state.loadingWatched, state.loadingList) {
        when {
            state.loadingList == LoadingState.Done -> onRemoveList()
            state.loadingWatched == LoadingState.Done -> when {
                state.isWatched -> onRemoveWatched()
                else -> onAddWatched()
            }
        }
    }

    LaunchedEffect(state.error) {
        if (state.error != null) {
            onError()
        }
    }

    ListItemContextViewContent(
        item = item,
        state = state,
        removable = removable,
        modifier = modifier,
        onWatchedClick = {
            when {
                state.isWatched -> confirmRemoveWatchedSheet = true
                else -> dateSheet = true
            }
        },
        onRemoveListClick = {
            confirmRemoveListSheet = true
        },
        onListsClick = onListsClick,
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
            item.subtitle(),
        ),
    )

    RemoveConfirmationSheet(
        active = confirmRemoveListSheet,
        onYes = {
            confirmRemoveListSheet = false
            viewModel.removeFromList()
        },
        onNo = { confirmRemoveListSheet = false },
        title = stringResource(R.string.button_text_remove_from_list),
        message = stringResource(
            R.string.warning_prompt_remove_from_personal_list,
            item.subtitle(),
            list.name,
        ),
    )

    DateSelectionSheet(
        active = dateSheet,
        title = item.subtitle(),
        onResult = viewModel::addToWatched,
        onDismiss = {
            dateSheet = false
        },
    )
}

@Composable
private fun ListItemContextViewContent(
    item: CustomListItem,
    state: ListItemContextState,
    removable: Boolean,
    modifier: Modifier = Modifier,
    onWatchedClick: () -> Unit = {},
    onRemoveListClick: () -> Unit = {},
    onListsClick: () -> Unit = {},
) {
    Column(
        verticalArrangement = spacedBy(0.dp),
        modifier = modifier,
    ) {
        Column(
            verticalArrangement = spacedBy(2.dp),
        ) {
            Text(
                text = item.title,
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
                text = item.subtitle(),
                color = TraktTheme.colors.textSecondary,
                style = TraktTheme.typography.paragraphSmaller,
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

        ActionButtons(
            item = item,
            state = state,
            removable = removable,
            onWatchedClick = onWatchedClick,
            onRemoveListClick = onRemoveListClick,
            onListsClick = onListsClick,
        )
    }
}

@Composable
private fun ActionButtons(
    item: CustomListItem,
    state: ListItemContextState,
    removable: Boolean,
    onWatchedClick: () -> Unit,
    onRemoveListClick: () -> Unit,
    onListsClick: () -> Unit,
) {
    val isLoadingOrDone =
        state.loadingWatched.isLoading ||
            state.loadingWatched.isDone ||
            state.loadingList.isLoading ||
            state.loadingList.isDone

    Column(
        verticalArrangement = spacedBy(TraktTheme.spacing.contextItemsSpace),
        modifier = Modifier.padding(top = 14.dp),
    ) {
        if (item is EpisodeItem && item.episode.rememberReleased()) {
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

        if (state.isAuthenticated && (item is EpisodeItem || item is SeasonItem)) {
            GhostButton(
                enabled = !isLoadingOrDone,
                text = stringResource(R.string.button_text_manage_lists),
                onClick = onListsClick,
                icon = painterResource(R.drawable.ic_lists_off),
                iconSize = 22.dp,
                iconSpace = 16.dp,
                modifier = Modifier
                    .graphicsLayer {
                        translationX = -6.dp.toPx()
                    },
            )
        }

        if (removable) {
            GhostButton(
                enabled = !isLoadingOrDone,
                loading = state.loadingList.isLoading || state.loadingList.isDone,
                text = stringResource(R.string.button_text_remove_from_list),
                onClick = onRemoveListClick,
                icon = painterResource(R.drawable.ic_minus),
                iconSize = 22.dp,
                iconSpace = 16.dp,
                modifier = Modifier
                    .graphicsLayer {
                        translationX = -6.dp.toPx()
                    },
            )
        }
    }
}

@Composable
private fun CustomListItem.subtitle(): String {
    return when (this) {
        is EpisodeItem -> when {
            episode.isSpoilerHidden() -> episode.seasonEpisodeString()
            else -> "${episode.seasonEpisodeString()} ${episode.title}"
        }
        is SeasonItem -> stringResource(R.string.text_season_number, season.number)
        is MovieItem, is ShowItem -> title
    }
}

@PreviewTraktLightDark
@Composable
private fun Preview() {
    TraktThemePreview {
        ListItemContextViewContent(
            item = EpisodeItem(
                show = PreviewData.show1,
                episode = PreviewData.episode1,
                itemId = 1,
                rank = 1,
                listedAt = Instant.now(),
            ),
            state = ListItemContextState(isAuthenticated = true),
            removable = true,
        )
    }
}
