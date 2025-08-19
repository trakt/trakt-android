package tv.trakt.trakt.ui.components.mediacards

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults.cardColors
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil3.ColorImage
import coil3.annotation.ExperimentalCoilApi
import coil3.compose.AsyncImage
import coil3.compose.AsyncImagePreviewHandler
import coil3.compose.LocalAsyncImagePreviewHandler
import coil3.request.ImageRequest
import coil3.request.crossfade
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.components.InfoChip
import tv.trakt.trakt.ui.theme.TraktTheme
import tv.trakt.trakt.ui.theme.VerticalImageAspectRatio

@Composable
internal fun VerticalMediaCard(
    title: String,
    imageUrl: String?,
    modifier: Modifier = Modifier,
    width: Dp = Dp.Unspecified,
    corner: Dp = 12.dp,
    chipContent: @Composable () -> Unit = {},
    onClick: () -> Unit = {},
) {
    val cardWidth = when {
        width != Dp.Unspecified -> width
        else -> TraktTheme.size.verticalMediaCardSize
    }

    var isError by remember(imageUrl) { mutableStateOf(false) }

    Column(
        verticalArrangement = Arrangement.spacedBy(10.dp),
        modifier = modifier
            .widthIn(max = cardWidth),
    ) {
        Card(
            onClick = onClick,
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(VerticalImageAspectRatio),
            shape = RoundedCornerShape(corner),
            colors = cardColors(
                containerColor = TraktTheme.colors.placeholderContainer,
            ),
            content = {
                Box(
                    modifier = Modifier.fillMaxSize(),
                ) {
                    if (imageUrl != null && !isError) {
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(imageUrl)
                                .crossfade(true)
                                .build(),
                            contentDescription = "Card image",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize(),
                            onError = { isError = true },
                        )
                    } else {
                        Image(
                            painter = painterResource(R.drawable.ic_placeholder_vertical_border),
                            contentDescription = title,
                            contentScale = ContentScale.Fit,
                            colorFilter = ColorFilter.tint(TraktTheme.colors.placeholderContent),
                            modifier = Modifier
                                .padding(5.dp)
                                .align(Alignment.Center),
                        )
                        Icon(
                            painter = painterResource(R.drawable.ic_placeholder_trakt),
                            contentDescription = title,
                            tint = TraktTheme.colors.placeholderContent,
                            modifier = Modifier
                                .size(80.dp)
                                .align(Alignment.TopEnd)
                                .graphicsLayer {
                                    translationX = 8.dp.toPx()
                                    translationY = -8.dp.toPx()
                                },
                        )

                        if ((imageUrl == null || isError) && title.isNotBlank()) {
                            Box(
                                modifier = Modifier
                                    .align(Alignment.BottomCenter)
                                    .fillMaxWidth()
                                    .fillMaxHeight(0.33f)
                                    .drawWithCache {
                                        onDrawBehind {
                                            drawRect(
                                                brush = Brush.verticalGradient(
                                                    0f to Color.Transparent,
                                                    1f to Color(0xFA212427),
                                                ),
                                            )
                                        }
                                    },
                            )
                        }

                        Text(
                            text = title.uppercase(),
                            style = TraktTheme.typography.buttonTertiary,
                            color = TraktTheme.colors.textPrimary,
                            textAlign = TextAlign.Center,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .padding(horizontal = 12.dp, vertical = 14.dp),
                        )
                    }
                }
            },
        )
        chipContent()
    }
}

@OptIn(ExperimentalCoilApi::class)
@Preview
@Composable
private fun PosterPreview() {
    TraktTheme {
        val previewHandler = AsyncImagePreviewHandler {
            ColorImage(Color.Blue.toArgb())
        }
        CompositionLocalProvider(LocalAsyncImagePreviewHandler provides previewHandler) {
            VerticalMediaCard(
                title = "Placeholder",
                imageUrl = "https://image.tmdb.org/t/p/w600_and_h900_bestv2/4iWjGghUj2uyHo2Hyw8NFBvsNGm.jpg",
                modifier = Modifier.padding(16.dp),
            )
        }
    }
}

@Preview
@Composable
private fun PosterPreviewPlaceholder() {
    TraktTheme {
        VerticalMediaCard(
            title = "Lorem",
            imageUrl = null,
            modifier = Modifier
                .padding(16.dp),
        )
    }
}

@Preview
@Composable
private fun PosterPreviewChipPlaceholder() {
    TraktTheme {
        VerticalMediaCard(
            title = "Placeholder",
            imageUrl = null,
            modifier = Modifier
                .padding(16.dp),
            chipContent = {
                InfoChip(
                    text = "Test",
                    iconPainter = painterResource(R.drawable.ic_clock),
                )
            },
        )
    }
}

@Preview
@Composable
private fun PosterPreviewError() {
    TraktTheme {
        VerticalMediaCard(
            title = "Placeholder",
            imageUrl = "https://example.com/not-a-poster.jpg",
            modifier = Modifier.padding(16.dp),
        )
    }
}
