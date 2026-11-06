package tv.trakt.trakt.core.settings.features.history

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.collections.immutable.persistentListOf
import tv.trakt.trakt.common.helpers.LoadingState
import tv.trakt.trakt.common.helpers.extensions.PreviewTraktLightDark
import tv.trakt.trakt.common.helpers.extensions.onClick
import tv.trakt.trakt.common.ui.theme.colors.Red500
import tv.trakt.trakt.core.settings.features.history.components.HistoryAnalysisCard
import tv.trakt.trakt.core.settings.features.history.model.CleanUpProgress
import tv.trakt.trakt.core.settings.features.history.model.CleanUpTarget
import tv.trakt.trakt.core.settings.features.history.model.PlayRetention
import tv.trakt.trakt.core.settings.features.history.model.PlaysSummary
import tv.trakt.trakt.helpers.SimpleScrollConnection
import tv.trakt.trakt.helpers.extensions.TraktThemePreview
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.components.ScrollableBackdropImage
import tv.trakt.trakt.ui.components.TraktHeader
import tv.trakt.trakt.ui.components.chips.FilterChip
import tv.trakt.trakt.ui.components.confirmation.ConfirmationSheet
import tv.trakt.trakt.ui.theme.TraktTheme

private val ROW_ITEM_SPACING = 12.dp
private val SECTION_SPACING = 16.dp

@Composable
internal fun HistoryAnalysisScreen(
    viewModel: HistoryAnalysisViewModel,
    onNavigateBack: () -> Unit,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    var confirmTarget by remember { mutableStateOf<CleanUpTarget?>(null) }
    var confirmLeave by remember { mutableStateOf(false) }

    BackHandler(enabled = state.isCleaning) {
        confirmLeave = true
    }

    HistoryAnalysisContent(
        state = state,
        onRetentionClick = viewModel::setRetention,
        onCleanUpClick = { confirmTarget = it },
        onBackClick = {
            if (state.isCleaning) {
                confirmLeave = true
            } else {
                onNavigateBack()
            }
        },
    )

    val target = confirmTarget
    val duplicates = when (target) {
        CleanUpTarget.Movies -> state.movies.duplicates
        CleanUpTarget.Episodes -> state.episodes.duplicates
        null -> 0
    }

    @OptIn(ExperimentalMaterial3Api::class)
    ConfirmationSheet(
        active = target != null,
        onYes = {
            confirmTarget = null
            target?.let(viewModel::startCleanUp)
        },
        onNo = {
            confirmTarget = null
        },
        title = stringResource(R.string.confirmation_title_clean_up_history),
        message = when (state.retention) {
            PlayRetention.Oldest -> stringResource(R.string.warning_prompt_clean_up_history, duplicates)
            PlayRetention.Newest -> stringResource(R.string.warning_prompt_clean_up_history_newest, duplicates)
        },
        yesText = stringResource(R.string.button_text_remove),
        yesColor = Red500,
    )

    @OptIn(ExperimentalMaterial3Api::class)
    ConfirmationSheet(
        active = confirmLeave,
        onYes = {
            confirmLeave = false
            viewModel.stopCleanUp()
            onNavigateBack()
        },
        onNo = {
            confirmLeave = false
        },
        title = stringResource(R.string.confirmation_title_leave_page),
        message = stringResource(R.string.warning_prompt_cancel_clear),
        yesText = stringResource(R.string.button_text_leave),
        noText = stringResource(R.string.button_text_stay),
        yesColor = Red500,
    )
}

@Composable
private fun HistoryAnalysisContent(
    state: HistoryAnalysisState,
    modifier: Modifier = Modifier,
    onRetentionClick: (PlayRetention) -> Unit = {},
    onCleanUpClick: (CleanUpTarget) -> Unit = {},
    onBackClick: () -> Unit = {},
) {
    val contentPadding = PaddingValues(
        start = TraktTheme.spacing.mainPageHorizontalSpace,
        end = TraktTheme.spacing.mainPageHorizontalSpace,
        top = WindowInsets.statusBars.asPaddingValues().calculateTopPadding().plus(4.dp),
        bottom = WindowInsets.navigationBars.asPaddingValues()
            .calculateBottomPadding()
            .plus(TraktTheme.size.navigationBarHeight)
            .plus(TraktTheme.spacing.mainPageBottomSpace),
    )

    val scrollConnection = rememberSaveable(saver = SimpleScrollConnection.Saver) {
        SimpleScrollConnection()
    }

    val loading = !state.loading.isDone
    val cleanUpEnabled = !loading && state.cleanUp?.isRunning != true

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(TraktTheme.colors.backgroundPrimary)
            .nestedScroll(scrollConnection),
    ) {
        ScrollableBackdropImage(
            translation = scrollConnection.resultOffset,
        )

        LazyColumn(
            contentPadding = contentPadding,
            overscrollEffect = null,
        ) {
            item {
                Row(
                    verticalAlignment = CenterVertically,
                    horizontalArrangement = spacedBy(ROW_ITEM_SPACING),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(TraktTheme.size.titleBarHeight)
                        .graphicsLayer { translationX = -2.dp.toPx() }
                        .onClick { onBackClick() },
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_back_arrow),
                        tint = TraktTheme.colors.textPrimary,
                        contentDescription = null,
                    )
                    TraktHeader(
                        title = stringResource(R.string.header_history_analysis),
                    )
                }
            }

            item {
                Text(
                    text = stringResource(R.string.description_history_analysis),
                    style = TraktTheme.typography.paragraphSmaller,
                    color = TraktTheme.colors.textSecondary,
                )
            }

            item {
                RetentionRow(
                    retention = state.retention,
                    enabled = state.cleanUp?.isRunning != true,
                    onClick = onRetentionClick,
                    modifier = Modifier
                        .padding(top = SECTION_SPACING, bottom = 4.dp),
                )
            }

            item {
                HistoryAnalysisCard(
                    label = stringResource(R.string.tag_text_movies),
                    summary = state.movies,
                    loading = loading,
                    cleanUp = state.cleanUp?.takeIf { it.target == CleanUpTarget.Movies },
                    cleanUpEnabled = cleanUpEnabled,
                    onCleanUpClick = { onCleanUpClick(CleanUpTarget.Movies) },
                    modifier = Modifier.padding(top = SECTION_SPACING),
                )
            }

            item {
                HistoryAnalysisCard(
                    label = stringResource(R.string.tag_text_episodes),
                    summary = state.episodes,
                    loading = loading,
                    cleanUp = state.cleanUp?.takeIf { it.target == CleanUpTarget.Episodes },
                    cleanUpEnabled = cleanUpEnabled,
                    onCleanUpClick = { onCleanUpClick(CleanUpTarget.Episodes) },
                    modifier = Modifier.padding(top = SECTION_SPACING),
                )
            }
        }
    }
}

@Composable
private fun RetentionRow(
    retention: PlayRetention,
    enabled: Boolean,
    onClick: (PlayRetention) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        verticalAlignment = CenterVertically,
        horizontalArrangement = spacedBy(TraktTheme.spacing.filterChipsSpace, Alignment.Start),
        modifier = modifier.fillMaxWidth(),
    ) {
        Text(
            text = stringResource(R.string.label_keep_play),
            style = TraktTheme.typography.paragraphSmaller,
            color = TraktTheme.colors.textSecondary,
            modifier = Modifier.padding(end = TraktTheme.spacing.filterChipsSpace),
        )
        FilterChip(
            selected = retention == PlayRetention.Oldest,
            text = stringResource(R.string.button_text_keep_oldest_play),
            onClick = { if (enabled) onClick(PlayRetention.Oldest) },
        )
        FilterChip(
            selected = retention == PlayRetention.Newest,
            text = stringResource(R.string.button_text_keep_newest_play),
            onClick = { if (enabled) onClick(PlayRetention.Newest) },
        )
    }
}

@PreviewTraktLightDark
@Composable
private fun Preview() {
    TraktThemePreview {
        HistoryAnalysisContent(
            state = HistoryAnalysisState(
                loading = LoadingState.Done,
                movies = PlaysSummary(
                    unique = 1240,
                    total = 1243,
                    duplicateIds = persistentListOf(1L, 2L, 3L),
                ),
                episodes = PlaysSummary(unique = 8400, total = 8400),
                cleanUp = CleanUpProgress(
                    target = CleanUpTarget.Movies,
                    processed = 1,
                    total = 3,
                ),
            ),
        )
    }
}
