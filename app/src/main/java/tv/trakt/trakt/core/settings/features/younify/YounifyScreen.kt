@file:OptIn(ExperimentalFoundationApi::class)

package tv.trakt.trakt.core.settings.features.younify

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import tv.trakt.trakt.common.helpers.LoadingState
import tv.trakt.trakt.common.helpers.extensions.onClick
import tv.trakt.trakt.common.ui.composables.FilmProgressIndicator
import tv.trakt.trakt.common.ui.theme.colors.Red400
import tv.trakt.trakt.helpers.SimpleScrollConnection
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.components.ScrollableBackdropImage
import tv.trakt.trakt.ui.components.TraktHeader
import tv.trakt.trakt.ui.theme.TraktTheme

@Composable
internal fun YounifyScreen(
    viewModel: YounifyViewModel,
    onNavigateBack: () -> Unit,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    YounifyScreenContent(
        state = state,
        onBackClick = onNavigateBack,
    )
}

@Composable
private fun YounifyScreenContent(
    state: YounifyState,
    modifier: Modifier = Modifier,
    onBackClick: () -> Unit = { },
) {
    val contentPadding = PaddingValues(
        start = TraktTheme.spacing.mainPageHorizontalSpace,
        end = TraktTheme.spacing.mainPageHorizontalSpace,
        top = WindowInsets.statusBars.asPaddingValues()
            .calculateTopPadding()
            .plus(4.dp),
        bottom = WindowInsets.navigationBars.asPaddingValues()
            .calculateBottomPadding()
            .plus(TraktTheme.size.navigationBarHeight * 2),
    )

    val scrollConnection = rememberSaveable(saver = SimpleScrollConnection.Saver) {
        SimpleScrollConnection()
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(TraktTheme.colors.backgroundPrimary)
            .nestedScroll(scrollConnection),
    ) {
        ScrollableBackdropImage(
            translation = scrollConnection.resultOffset,
        )

        Column(
            modifier = Modifier
                .padding(contentPadding)
                .verticalScroll(rememberScrollState()),
        ) {
            TitleBar(
                modifier = Modifier
                    .onClick {
                        onBackClick()
                    },
            )

            state.error?.let { error ->
                Text(
                    text = error.message ?: stringResource(R.string.error_text_unexpected_error_short),
                    style = TraktTheme.typography.paragraphSmaller,
                    color = Red400,
                    modifier = Modifier
                        .padding(top = 16.dp),
                )
            }

            if (state.loading.isDone && !state.younifyServices.isNullOrEmpty()) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(32.dp),
                    modifier = Modifier.padding(top = 24.dp),
                ) {
                    state.younifyServices.forEach { service ->
                        Text(
                            text = service.name,
                            style = TraktTheme.typography.buttonSecondary,
                            color = TraktTheme.colors.textPrimary,
                        )
                    }
                }
            }
        }

        if (state.loading == LoadingState.LOADING) {
            FilmProgressIndicator(
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(contentPadding),
            )
        }
    }
}

@Composable
private fun TitleBar(modifier: Modifier = Modifier) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = modifier
            .fillMaxWidth(),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier
                .height(TraktTheme.size.titleBarHeight)
                .graphicsLayer {
                    translationX = -2.dp.toPx()
                },
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_back_arrow),
                tint = TraktTheme.colors.textPrimary,
                contentDescription = null,
            )
            TraktHeader(
                title = stringResource(R.string.header_settings_automatic_tracking),
            )
        }
    }
}

// Previews

@Preview(
    device = "id:pixel_6",
    showBackground = true,
    backgroundColor = 0xFF131517,
)
@Composable
private fun Preview() {
    TraktTheme {
        YounifyScreenContent(
            state = YounifyState(),
        )
    }
}
