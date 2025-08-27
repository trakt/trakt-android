package tv.trakt.trakt.ui.components.mediacards

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Arrangement.Absolute.spacedBy
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.width
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
import androidx.compose.ui.graphics.Brush.Companion.verticalGradient
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
import tv.trakt.trakt.helpers.preview.PreviewData
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.components.InfoChip
import tv.trakt.trakt.ui.theme.HorizontalImageAspectRatio
import tv.trakt.trakt.ui.theme.TraktTheme

@Composable
internal fun HorizontalMediaCard(
    title: String,
    modifier: Modifier = Modifier,
    containerImageUrl: String? = null,
    contentImageUrl: String? = null,
    paletteColor: Color? = null,
    corner: Dp = 12.dp,
    onClick: () -> Unit = {},
    footerContent: @Composable () -> Unit = {},
    cardContent: @Composable () -> Unit = {},
    cardTopContent: @Composable () -> Unit = {},
) {
    var isContainerError by remember(containerImageUrl) { mutableStateOf(false) }
    var isContentError by remember(contentImageUrl) { mutableStateOf(false) }

    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = modifier
            .sizeIn(maxWidth = TraktTheme.size.horizontalMediaCardSize),
    ) {
        Card(
            onClick = onClick,
            modifier = Modifier
                .width(TraktTheme.size.horizontalMediaCardSize)
                .aspectRatio(HorizontalImageAspectRatio),
            shape = RoundedCornerShape(corner),
            colors = cardColors(
                containerColor = TraktTheme.colors.placeholderContainer,
            ),
            content = {
                Box(
                    modifier = Modifier.fillMaxSize(),
                ) {
                    val isErrorContainer = containerImageUrl == null || isContainerError
                    val isErrorContent = contentImageUrl == null || isContentError

                    if (!isErrorContainer) {
                        AsyncImage(
                            model = containerImageUrl,
                            contentDescription = title,
                            contentScale = ContentScale.Crop,
                            onError = { isContainerError = true },
                            modifier = Modifier
                                .align(Alignment.Center)
                                .fillMaxSize(),
                        )
                    } else {
                        Image(
                            painter = painterResource(R.drawable.ic_placeholder_horizontal_border),
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
                                    translationX = -22.dp.toPx()
                                    translationY = -18.dp.toPx()
                                },
                        )
                    }

                    if (isErrorContent && title.isNotBlank()) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .fillMaxWidth()
                                .fillMaxHeight(0.66F)
                                .drawWithCache {
                                    onDrawBehind {
                                        drawRect(
                                            brush = verticalGradient(
                                                0f to Color.Transparent,
                                                1f to Color(0xFA212427),
                                            ),
                                        )
                                    }
                                },
                        )
                    }

                    if (paletteColor != null && !isErrorContainer) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .fillMaxWidth()
                                .fillMaxHeight(0.66F)
                                .drawWithCache {
                                    onDrawBehind {
                                        drawRect(
                                            brush = verticalGradient(
                                                0f to Color.Transparent,
                                                1f to paletteColor.copy(alpha = 0.9F),
                                            ),
                                        )
                                    }
                                },
                        )
                    }

                    if (contentImageUrl != null && !isContentError) {
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(contentImageUrl)
                                .crossfade(true)
                                .build(),
                            contentDescription = title,
                            contentScale = ContentScale.Fit,
                            onError = { isContentError = true },
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .padding(bottom = 4.dp)
                                .widthIn(max = 110.dp),
                        )
                    } else {
                        Text(
                            text = title.uppercase(),
                            style = TraktTheme.typography.buttonTertiary,
                            color = TraktTheme.colors.textPrimary,
                            textAlign = TextAlign.Center,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .padding(
                                    horizontal = 13.dp,
                                    vertical = if (containerImageUrl == null || isContainerError) 14.dp else 8.dp,
                                ),
                        )
                    }

                    Box(
                        modifier = Modifier
                            .padding(6.dp)
                            .align(Alignment.BottomStart),
                    ) {
                        cardContent()
                    }

                    Box(
                        modifier = Modifier
                            .padding(6.dp)
                            .align(Alignment.TopEnd),
                    ) {
                        cardTopContent()
                    }
                }
            },
        )

        Column(
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            footerContent()
        }
    }
}

@OptIn(ExperimentalCoilApi::class)
@Preview(widthDp = 160)
@Composable
private fun Preview() {
    TraktTheme {
        val previewHandler = AsyncImagePreviewHandler {
            ColorImage(Color.LightGray.toArgb())
        }
        CompositionLocalProvider(LocalAsyncImagePreviewHandler provides previewHandler) {
            Column(
                verticalArrangement = spacedBy(16.dp),
            ) {
                HorizontalMediaCard(
                    title = "The Mandalorian",
                    containerImageUrl = PreviewData.show1.images?.getFanartUrl(),
                    contentImageUrl = PreviewData.show1.images?.getLogoUrl(),
                    onClick = {},
                    cardContent = {
                        InfoChip(
                            text = "1h 45m",
                            iconPainter = painterResource(R.drawable.ic_clock),
                            modifier = Modifier,
                        )
                    },
                    footerContent = {
                        InfoChip(
                            text = "1h 45m",
                            iconPainter = painterResource(R.drawable.ic_clock),
                            modifier = Modifier,
                        )
                    },
                )

                HorizontalMediaCard(
                    title = "Empty",
                    containerImageUrl = PreviewData.show1.images?.getFanartUrl(),
                    contentImageUrl = PreviewData.show1.images?.getLogoUrl(),
                    paletteColor = Color(0xFF292525),
                    onClick = {},
                )

                HorizontalMediaCard(
                    title = "The Witcher",
                    containerImageUrl = null,
                    contentImageUrl = null,
                    onClick = {},
                )
            }
        }
    }
}
