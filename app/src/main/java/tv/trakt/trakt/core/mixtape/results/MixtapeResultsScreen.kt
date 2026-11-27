package tv.trakt.trakt.core.mixtape.results

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentMapOf
import kotlinx.collections.immutable.toImmutableList
import tv.trakt.trakt.LocalSnackbarState
import tv.trakt.trakt.common.helpers.extensions.PreviewTraktLightDark
import tv.trakt.trakt.common.helpers.preview.PreviewData
import tv.trakt.trakt.common.model.MediaType
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.model.mixtape.Mixtape.Suggestion
import tv.trakt.trakt.common.model.mixtape.MixtapeItem
import tv.trakt.trakt.common.ui.composables.FilmProgressIndicator
import tv.trakt.trakt.core.mixtape.model.MixtapeTarget
import tv.trakt.trakt.core.mixtape.results.MixtapeResultsEvent.KeepFailed
import tv.trakt.trakt.core.mixtape.results.MixtapeResultsEvent.MoveFailed
import tv.trakt.trakt.core.mixtape.results.MixtapeResultsEvent.Moved
import tv.trakt.trakt.core.mixtape.results.ui.MixtapeDeck
import tv.trakt.trakt.core.mixtape.results.ui.MixtapeDone
import tv.trakt.trakt.core.mixtape.results.ui.MixtapeMoveSheet
import tv.trakt.trakt.core.mixtape.results.ui.displayName
import tv.trakt.trakt.core.mixtape.ui.HideBottomBar
import tv.trakt.trakt.helpers.extensions.TraktThemePreview
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.components.buttons.GhostButton
import tv.trakt.trakt.ui.components.buttons.PrimaryButton
import tv.trakt.trakt.ui.theme.TraktTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun MixtapeResultsScreen(
    viewModel: MixtapeResultsViewModel,
    onNavigateBack: () -> Unit,
    onNavigateClose: () -> Unit,
    onNavigateStartOver: (MediaType) -> Unit,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbar = LocalSnackbarState.current
    val resources = LocalResources.current
    var moveSheet by remember { mutableStateOf(false) }

    HideBottomBar()

    BackHandler(onBack = onNavigateClose)

    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            val watchlistName = resources.getString(R.string.page_title_watchlist)
            when (event) {
                is Moved -> {
                    val result = snackbar.showSnackbar(
                        message = resources.getString(
                            R.string.text_mixtape_moved,
                            event.count,
                            event.move.to.displayName(watchlistName),
                        ),
                        actionLabel = resources.getString(R.string.button_text_undo),
                        duration = SnackbarDuration.Long,
                    )
                    if (result == SnackbarResult.ActionPerformed) viewModel.undoMove(event.move)
                }
                is MoveFailed -> {
                    snackbar.showSnackbar(resources.getString(R.string.text_mixtape_move_failed))
                }
                is KeepFailed -> {
                    snackbar.showSnackbar(resources.getString(R.string.text_mixtape_keep_failed, event.title))
                }
            }
        }
    }

    MixtapeResultsContent(
        state = state,
        onKeep = viewModel::keep,
        onRetryClick = viewModel::retry,
        onSkip = viewModel::skip,
        onOpen = viewModel::open,
        onFindMoreClick = viewModel::findMore,
        onSavedToClick = {
            snackbar.currentSnackbarData?.dismiss()
            moveSheet = true
        },
        onChangePicksClick = onNavigateBack,
        onStartOverClick = { onNavigateStartOver(state.type) },
        onCloseClick = onNavigateClose,
    )

    MixtapeMoveSheet(
        active = moveSheet,
        current = state.savedTo,
        lists = state.lists,
        suggestedName = stringResource(
            R.string.text_mixtape_new_list_name,
            state.pickTitles.values.joinToString(" + "),
        ),
        moving = state.moving,
        onMove = viewModel::move,
        onMoveNew = viewModel::moveToNewList,
        onDismiss = { moveSheet = false },
    )
}

@Composable
private fun MixtapeResultsContent(
    state: MixtapeResultsState,
    modifier: Modifier = Modifier,
    onKeep: () -> Unit = {},
    onSkip: () -> Unit = {},
    onOpen: () -> Unit = {},
    onFindMoreClick: () -> Unit = {},
    onSavedToClick: () -> Unit = {},
    onRetryClick: () -> Unit = {},
    onChangePicksClick: () -> Unit = {},
    onStartOverClick: () -> Unit = {},
    onCloseClick: () -> Unit = {},
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .fillMaxSize()
            .background(TraktTheme.colors.backgroundPrimary),
    ) {
        val current = state.current
        when {
            state.loading -> FilmProgressIndicator()
            state.error != null && state.slots.isEmpty() -> ErrorResults(
                onRetryClick = onRetryClick,
                onChangePicksClick = onChangePicksClick,
            )
            state.isEmpty -> EmptyResults(onChangePicksClick = onChangePicksClick)
            state.isDone || current == null -> MixtapeDone(
                state = state,
                onSavedToClick = onSavedToClick,
                onFindMoreClick = onFindMoreClick,
                onChangePicksClick = onChangePicksClick,
                onStartOverClick = onStartOverClick,
                onCloseClick = onCloseClick,
            )
            else -> MixtapeDeck(
                state = state,
                current = current,
                onKeep = onKeep,
                onSkip = onSkip,
                onOpen = onOpen,
                onBackClick = onCloseClick,
            )
        }
    }
}

@Composable
private fun ErrorResults(
    onRetryClick: () -> Unit,
    onChangePicksClick: () -> Unit,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = spacedBy(20.dp),
        modifier = Modifier.padding(horizontal = TraktTheme.spacing.mainPageHorizontalSpace),
    ) {
        Text(
            text = stringResource(R.string.error_text_unexpected_error_short),
            style = TraktTheme.typography.paragraph,
            color = TraktTheme.colors.textSecondary,
            textAlign = TextAlign.Center,
        )
        PrimaryButton(
            text = stringResource(R.string.button_text_retry),
            onClick = onRetryClick,
        )
        GhostButton(
            text = stringResource(R.string.button_text_mixtape_change_picks),
            onClick = onChangePicksClick,
        )
    }
}

@Composable
private fun EmptyResults(onChangePicksClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = spacedBy(20.dp),
        modifier = Modifier.padding(horizontal = TraktTheme.spacing.mainPageHorizontalSpace),
    ) {
        Text(
            text = stringResource(R.string.text_mixtape_results_empty),
            style = TraktTheme.typography.paragraph,
            color = TraktTheme.colors.textSecondary,
            textAlign = TextAlign.Center,
        )
        PrimaryButton(
            text = stringResource(R.string.button_text_mixtape_change_picks),
            onClick = onChangePicksClick,
        )
    }
}

@PreviewTraktLightDark
@Composable
private fun EmptyPreview() {
    TraktThemePreview {
        MixtapeResultsContent(
            state = MixtapeResultsState(loading = false),
        )
    }
}

@PreviewTraktLightDark
@Composable
private fun LoadingPreview() {
    TraktThemePreview {
        MixtapeResultsContent(
            state = MixtapeResultsState(),
        )
    }
}

private val previewSuggestions = persistentListOf(
    Suggestion(
        item = MixtapeItem.ShowItem(PreviewData.show1),
        because = PreviewData.show2.ids.trakt,
        match = 88,
    ),
    Suggestion(
        item = MixtapeItem.MovieItem(PreviewData.movie1),
        because = PreviewData.show2.ids.trakt,
        match = 81,
    ),
    Suggestion(
        item = MixtapeItem.MovieItem(PreviewData.movie2),
        because = PreviewData.show2.ids.trakt,
        match = 74,
    ),
)

private val previewState = MixtapeResultsState(
    loading = false,
    suggestions = previewSuggestions,
    slots = previewSuggestions,
    pickTitles = persistentMapOf(PreviewData.show2.ids.trakt to PreviewData.show2.title),
)

@PreviewTraktLightDark
@Composable
private fun DeckPreview() {
    TraktThemePreview {
        MixtapeResultsContent(
            state = previewState,
        )
    }
}

@PreviewTraktLightDark
@Composable
private fun DonePreview() {
    TraktThemePreview {
        MixtapeResultsContent(
            state = previewState.copy(
                rounds = persistentListOf(
                    persistentListOf(previewSuggestions[0].item),
                    previewSuggestions.map { it.item }.toImmutableList(),
                ),
                savedTo = MixtapeTarget.PersonalList(
                    id = TraktId(1),
                    ownerId = TraktId(1),
                    name = "Weekend picks",
                ),
            ),
        )
    }
}
