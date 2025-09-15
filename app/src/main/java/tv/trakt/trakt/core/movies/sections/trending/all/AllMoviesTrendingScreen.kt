package tv.trakt.trakt.core.movies.sections.trending.all

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement.Absolute.spacedBy
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import tv.trakt.trakt.common.helpers.extensions.onClick
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.components.BackdropImage
import tv.trakt.trakt.ui.theme.TraktTheme

@Composable
internal fun AllMoviesScreen(
    viewModel: AllMoviesTrendingViewModel,
    onNavigateBack: () -> Unit,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    AllMoviesScreenContent(
        state = state,
        onBackClick = onNavigateBack,
    )
}

@Composable
private fun AllMoviesScreenContent(
    state: AllMoviesTrendingState,
    modifier: Modifier = Modifier,
    onBackClick: () -> Unit = {},
) {
    val topInset = WindowInsets.statusBars.asPaddingValues()
        .calculateTopPadding()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(TraktTheme.colors.backgroundPrimary),
    ) {
        BackdropImage(
            imageUrl = state.backgroundUrl,
        )

        Row(
            verticalAlignment = CenterVertically,
            horizontalArrangement = spacedBy(12.dp),
            modifier = Modifier
                .padding(top = topInset)
                .height(TraktTheme.size.titleBarHeight)
                .onClick(onBackClick),
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_back_arrow),
                tint = Color.White,
                contentDescription = "Back",
                modifier = Modifier
                    .padding(
                        start = TraktTheme.spacing.mainPageHorizontalSpace - 2.dp,
                    ),
            )
            Text(
                text = stringResource(R.string.list_title_trending_movies),
                color = TraktTheme.colors.textPrimary,
                style = TraktTheme.typography.heading5,
            )
        }
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
        AllMoviesScreenContent(
            state = AllMoviesTrendingState(),
        )
    }
}
