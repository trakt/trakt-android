package tv.trakt.trakt.core.mixtape.results

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import tv.trakt.trakt.common.helpers.extensions.PreviewTraktLightDark
import tv.trakt.trakt.common.ui.composables.FilmProgressIndicator
import tv.trakt.trakt.core.mixtape.results.ui.MixtapeDeck
import tv.trakt.trakt.core.mixtape.results.ui.MixtapeDone
import tv.trakt.trakt.core.mixtape.ui.HideBottomBar
import tv.trakt.trakt.helpers.extensions.TraktThemePreview
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.components.buttons.PrimaryButton
import tv.trakt.trakt.ui.theme.TraktTheme

@Composable
internal fun MixtapeResultsScreen(
    viewModel: MixtapeResultsViewModel,
    onNavigateBack: () -> Unit,
    onNavigateClose: () -> Unit,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    HideBottomBar()

    BackHandler(onBack = onNavigateClose)

    MixtapeResultsContent(
        state = state,
        onKeep = viewModel::keep,
        onSkip = viewModel::skip,
        onOpen = viewModel::open,
        onFindMoreClick = viewModel::findMore,
        onChangePicksClick = onNavigateBack,
        onCloseClick = onNavigateClose,
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
    onChangePicksClick: () -> Unit = {},
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
            state.isEmpty -> EmptyResults(onChangePicksClick = onChangePicksClick)
            state.isDone || current == null -> MixtapeDone(
                state = state,
                onFindMoreClick = onFindMoreClick,
                onChangePicksClick = onChangePicksClick,
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
