@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)

package tv.trakt.trakt.core.summary.movies

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement.Absolute.spacedBy
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.layout.LazyLayoutCacheWindow
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.TopCenter
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import tv.trakt.trakt.common.Config.WEB_V3_BASE_URL
import tv.trakt.trakt.common.model.Images
import tv.trakt.trakt.common.model.Movie
import tv.trakt.trakt.core.summary.ui.DetailsActions
import tv.trakt.trakt.core.summary.ui.DetailsBackground
import tv.trakt.trakt.core.summary.ui.DetailsHeader
import tv.trakt.trakt.helpers.SimpleScrollConnection
import tv.trakt.trakt.helpers.preview.PreviewData
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.theme.TraktTheme

@Composable
internal fun MovieDetailsScreen(
    modifier: Modifier = Modifier,
    viewModel: MovieDetailsViewModel,
    onNavigateBack: () -> Unit,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    MovieDetailsContent(
        state = state,
        modifier = modifier,
        onBackClick = onNavigateBack,
    )
}

@Composable
internal fun MovieDetailsContent(
    state: MovieDetailsState,
    modifier: Modifier = Modifier,
    onBackClick: () -> Unit = {},
) {
    val context = LocalContext.current
    val uriHandler = LocalUriHandler.current

    val contentPadding = PaddingValues(
        top = WindowInsets.statusBars.asPaddingValues()
            .calculateTopPadding()
            .plus(16.dp),
        bottom = WindowInsets.navigationBars.asPaddingValues()
            .calculateBottomPadding()
            .plus(TraktTheme.size.navigationBarHeight * 2),
    )

    val listState = rememberLazyListState(
        cacheWindow = LazyLayoutCacheWindow(
            aheadFraction = 0.5F,
            behindFraction = 0.5F,
        ),
    )

    val listScrollConnection = remember {
        SimpleScrollConnection()
    }

    Box(
        contentAlignment = TopCenter,
        modifier = modifier
            .fillMaxSize()
            .background(TraktTheme.colors.backgroundPrimary)
            .nestedScroll(listScrollConnection),
    ) {
        state.movie?.let { movie ->
            DetailsBackground(
                imageUrl = movie.images?.getFanartUrl(Images.Size.THUMB),
                color = movie.colors?.colors?.second,
                translation = listScrollConnection.resultOffset,
            )

            LazyColumn(
                state = listState,
                verticalArrangement = spacedBy(0.dp),
                contentPadding = contentPadding,
                overscrollEffect = null,
            ) {
                item {
                    DetailsHeader(
                        movie = movie,
                        ratings = state.movieRatings,
                        onBackClick = onBackClick,
                        onTrailerClick = {
                            movie.trailer?.let { uriHandler.openUri(it) }
                        },
                        onShareClick = { shareMovie(movie, context) },
                        modifier = Modifier.align(Alignment.Center),
                    )
                }

                item {
                    DetailsActions(
                        onPrimaryClick = {
                        },
                        modifier = Modifier
                            .align(Alignment.Center)
                            .fillMaxWidth()
                            .padding(top = 16.dp)
                            .padding(horizontal = 42.dp),
                    )
                }

                item {
                    Spacer(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(500.dp),
                    )
                }
            }
        }
    }
}

private fun shareMovie(
    movie: Movie,
    context: Context,
) {
    val shareText = "${context.getString(R.string.text_share_movie, movie.title)} " +
        "${WEB_V3_BASE_URL}movies/${movie.ids.slug.value}"

    val intent = Intent().apply {
        action = Intent.ACTION_SEND
        putExtra(Intent.EXTRA_TEXT, shareText)
        type = "text/plain"
    }

    context.startActivity(Intent.createChooser(intent, movie.title))
}

@Preview(
    device = "id:pixel_5",
    showBackground = true,
    backgroundColor = 0xFF131517,
)
@Composable
private fun Preview() {
    TraktTheme {
        MovieDetailsContent(
            state = MovieDetailsState(
                movie = PreviewData.movie1,
            ),
        )
    }
}
