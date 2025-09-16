@file:OptIn(ExperimentalFoundationApi::class)

package tv.trakt.trakt.core.shows.sections.recommended.all

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement.Absolute.spacedBy
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.collections.immutable.toImmutableList
import tv.trakt.trakt.common.helpers.extensions.onClick
import tv.trakt.trakt.common.model.Show
import tv.trakt.trakt.core.shows.ui.AllShowsListView
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.components.BackdropImage
import tv.trakt.trakt.ui.theme.TraktTheme

@Composable
internal fun AllShowsRecommendedScreen(
    viewModel: AllShowsRecommendedViewModel,
    onNavigateBack: () -> Unit,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    AllShowsRecommendedScreenContent(
        state = state,
        onBackClick = onNavigateBack,
    )
}

@Composable
private fun AllShowsRecommendedScreenContent(
    state: AllShowsRecommendedState,
    modifier: Modifier = Modifier,
    onBackClick: () -> Unit = {},
) {
    val gridState = rememberLazyGridState()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(TraktTheme.colors.backgroundPrimary),
    ) {
        BackdropImage(
            imageUrl = state.backgroundUrl,
        )

        AllShowsListView(
            title = {
                TitleBar(
                    modifier = Modifier
                        .padding(bottom = 2.dp)
                        .onClick(onBackClick),
                )
            },
            state = gridState,
            loading = state.loading.isLoading,
            items = state.items ?: emptyList<Show>().toImmutableList(),
            onEndOfList = {
                // No pagination support for recommended shows
            },
        )
    }
}

@Composable
private fun TitleBar(modifier: Modifier = Modifier) {
    Row(
        verticalAlignment = CenterVertically,
        horizontalArrangement = spacedBy(12.dp),
        modifier = modifier
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
        Text(
            text = stringResource(R.string.list_title_recommended_shows),
            color = TraktTheme.colors.textPrimary,
            style = TraktTheme.typography.heading5,
        )
    }
}

@Preview(
    device = "id:pixel_6",
    showBackground = true,
    backgroundColor = 0xFF131517,
)
@Composable
private fun Preview() {
    TraktTheme {
        AllShowsRecommendedScreenContent(
            state = AllShowsRecommendedState(),
        )
    }
}
