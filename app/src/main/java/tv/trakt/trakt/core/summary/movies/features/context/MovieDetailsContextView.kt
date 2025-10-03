@file:OptIn(ExperimentalMaterial3Api::class)

package tv.trakt.trakt.core.summary.movies.features.context

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
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow.Companion.Ellipsis
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.ColorImage
import coil3.annotation.ExperimentalCoilApi
import coil3.compose.AsyncImagePreviewHandler
import coil3.compose.LocalAsyncImagePreviewHandler
import tv.trakt.trakt.common.model.Movie
import tv.trakt.trakt.common.ui.theme.colors.Shade910
import tv.trakt.trakt.helpers.preview.PreviewData
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.components.buttons.GhostButton
import tv.trakt.trakt.ui.theme.TraktTheme

@Composable
internal fun MovieDetailsContextView(
    movie: Movie,
    modifier: Modifier = Modifier,
    onShareClick: (() -> Unit)? = null,
    onTrailerClick: (() -> Unit)? = null,
) {
    MovieDetailsContextViewContent(
        movie = movie,
        onShareClick = onShareClick,
        onTrailerClick = onTrailerClick,
        modifier = modifier,
    )
}

@Composable
private fun MovieDetailsContextViewContent(
    movie: Movie,
    modifier: Modifier = Modifier,
    onShareClick: (() -> Unit)? = null,
    onTrailerClick: (() -> Unit)? = null,
) {
    val genresText = remember(movie.genres) {
        movie.genres.take(3).joinToString(", ") { genre ->
            genre.replaceFirstChar {
                it.uppercaseChar()
            }
        }
    }

    Column(
        verticalArrangement = spacedBy(0.dp),
        modifier = modifier,
    ) {
        Text(
            text = movie.title,
            color = TraktTheme.colors.textPrimary,
            style = TraktTheme.typography.heading2,
            maxLines = 1,
            overflow = Ellipsis,
            autoSize = TextAutoSize.StepBased(
                maxFontSize = TraktTheme.typography.heading2.fontSize,
                minFontSize = 16.sp,
                stepSize = 2.sp,
            ),
        )

        Text(
            text = "${movie.released?.year ?: movie.year}  •  $genresText",
            color = TraktTheme.colors.textSecondary,
            style = TraktTheme.typography.paragraphSmaller,
            maxLines = 1,
            overflow = Ellipsis,
            modifier = Modifier
                .padding(top = 2.dp),
        )

//        movie.status?.let {
//            Text(
//                text = it.uppercase(),
//                color = when (it.lowercase()) {
//                    "canceled", "ended" -> Red500
//                    else -> Purple300
//                },
//                style = TraktTheme.typography.meta,
//                modifier = Modifier
//                    .padding(top = 6.dp),
//            )
//        }

        Spacer(
            modifier = Modifier
                .padding(top = 22.dp)
                .background(Shade910)
                .fillMaxWidth()
                .height(1.dp),
        )

        ActionButtons(
            trailerEnabled = !movie.trailer.isNullOrBlank(),
            onShareClick = onShareClick,
            onTrailerClick = onTrailerClick,
            modifier = Modifier
                .padding(top = 12.dp),
        )
    }
}

@Composable
private fun ActionButtons(
    modifier: Modifier = Modifier,
    trailerEnabled: Boolean = true,
    onShareClick: (() -> Unit)? = null,
    onTrailerClick: (() -> Unit)? = null,
) {
    Column(
        verticalArrangement = spacedBy(TraktTheme.spacing.contextItemsSpace),
        modifier = modifier
            .graphicsLayer {
                translationX = -4.dp.toPx()
            },
    ) {
        GhostButton(
            text = stringResource(R.string.button_text_share),
            icon = painterResource(R.drawable.ic_share),
            iconSize = 25.dp,
            iconSpace = 14.dp,
            modifier = Modifier
                .graphicsLayer {
                    translationX = -6.dp.toPx()
                },
            onClick = onShareClick ?: {},
        )
        GhostButton(
            enabled = trailerEnabled,
            text = stringResource(R.string.button_text_trailer),
            icon = painterResource(R.drawable.ic_trailer),
            iconSize = 21.dp,
            iconSpace = 15.dp,
            modifier = Modifier
                .graphicsLayer {
                    translationX = -3.dp.toPx()
                },
            onClick = onTrailerClick ?: {},
        )
    }
}

@OptIn(ExperimentalCoilApi::class)
@Preview(
    device = "id:pixel_5",
    showBackground = true,
    backgroundColor = 0xFF212427,
)
@Composable
private fun Preview() {
    TraktTheme {
        val previewHandler = AsyncImagePreviewHandler {
            ColorImage(Color.Blue.toArgb())
        }
        CompositionLocalProvider(LocalAsyncImagePreviewHandler provides previewHandler) {
            MovieDetailsContextViewContent(
                movie = PreviewData.movie1,
            )
        }
    }
}
