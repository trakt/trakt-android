package tv.trakt.trakt.core.mixtape

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import tv.trakt.trakt.LocalBottomBarVisibility
import tv.trakt.trakt.common.helpers.extensions.PreviewTraktLightDark
import tv.trakt.trakt.common.helpers.extensions.onClick
import tv.trakt.trakt.common.model.MediaType
import tv.trakt.trakt.helpers.extensions.TraktThemePreview
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.components.TraktHeader
import tv.trakt.trakt.ui.theme.TraktTheme

private val ROW_ITEM_SPACING = 12.dp

@Composable
internal fun MixtapeScreen(
    viewModel: MixtapeViewModel,
    onNavigateBack: () -> Unit,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val bottomBarVisibility = LocalBottomBarVisibility.current

    DisposableEffect(Unit) {
        bottomBarVisibility.value = false
        onDispose {
            bottomBarVisibility.value = true
        }
    }

    MixtapeContent(
        state = state,
        onBackClick = onNavigateBack,
    )
}

@Composable
private fun MixtapeContent(
    state: MixtapeState,
    modifier: Modifier = Modifier,
    onBackClick: () -> Unit = {},
) {
    val contentPadding = PaddingValues(
        start = TraktTheme.spacing.mainPageHorizontalSpace,
        end = TraktTheme.spacing.mainPageHorizontalSpace,
        top = WindowInsets.statusBars.asPaddingValues().calculateTopPadding().plus(4.dp),
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(TraktTheme.colors.backgroundPrimary)
            .padding(contentPadding),
    ) {
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
                title = stringResource(R.string.page_title_mixtape),
            )
        }

        Text(
            text = stringResource(
                when (state.type) {
                    MediaType.Show -> R.string.text_mixtape_description_shows
                    MediaType.Movie,
                    MediaType.Season,
                    MediaType.Episode,
                    -> R.string.text_mixtape_description_movies
                },
            ),
            style = TraktTheme.typography.paragraphSmall,
            color = TraktTheme.colors.textSecondary,
        )
    }
}

@PreviewTraktLightDark
@Composable
private fun Preview() {
    TraktThemePreview {
        MixtapeContent(
            state = MixtapeState(),
        )
    }
}
