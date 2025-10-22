package tv.trakt.trakt.ui.components.mediacards

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Arrangement.Absolute.spacedBy
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.dropShadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Brush.Companion.linearGradient
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.shadow.Shadow
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.ColorImage
import coil3.annotation.ExperimentalCoilApi
import coil3.compose.AsyncImage
import coil3.compose.AsyncImagePreviewHandler
import coil3.compose.LocalAsyncImagePreviewHandler
import coil3.request.ImageRequest
import coil3.request.crossfade
import tv.trakt.trakt.common.helpers.extensions.onClick
import tv.trakt.trakt.common.ui.theme.colors.Purple500
import tv.trakt.trakt.common.ui.theme.colors.Shade940
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.components.InfoChip
import tv.trakt.trakt.ui.theme.TraktTheme
import tv.trakt.trakt.ui.theme.VerticalImageAspectRatio

@Composable
internal fun PanelMediaCard(
    title: String,
    titleOriginal: String?,
    subtitle: String,
    contentImageUrl: String?,
    containerImageUrl: String?,
    modifier: Modifier = Modifier,
    corner: Dp = 16.dp,
    shadow: Dp = 0.dp,
    more: Boolean = true,
    containerColor: Color = TraktTheme.colors.panelCardContainer,
    onClick: (() -> Unit)? = null,
    onLongClick: (() -> Unit)? = null,
    onImageClick: (() -> Unit)? = null,
    footerContent: @Composable (() -> Unit)? = null,
) {
    var isPosterError by remember { mutableStateOf(false) }
    var isContainerError by remember { mutableStateOf(false) }

    Row(
        horizontalArrangement = spacedBy(0.dp),
        verticalAlignment = Alignment.Top,
        modifier = modifier
            .dropShadow(
                shape = RoundedCornerShape(corner),
                shadow = Shadow(
                    radius = shadow,
                    color = Shade940,
                    spread = 2.dp,
                    alpha = if (shadow > 0.dp) 0.33F else 0F,
                ),
            )
            .clip(RoundedCornerShape(corner))
            .background(containerColor)
            .height(TraktTheme.size.verticalMediumMediaCardSize / VerticalImageAspectRatio)
            .combinedClickable(
                onClick = onClick ?: {},
                onLongClick = when {
                    onLongClick != null -> onLongClick
                    else -> null
                },
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(),
            ),
    ) {
        if (!contentImageUrl.isNullOrBlank() && !isPosterError) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(contentImageUrl)
                    .crossfade(true)
                    .build(),
                contentDescription = "Card image",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .padding(start = 4.dp)
                    .padding(vertical = 4.dp)
                    .aspectRatio(VerticalImageAspectRatio)
                    .width(TraktTheme.size.verticalMediumMediaCardSize)
                    .clip(RoundedCornerShape(corner - 2.dp))
                    .onClick(onClick = onImageClick ?: {}),
            )
        } else {
            Box(
                modifier = Modifier
                    .padding(start = 4.dp)
                    .padding(vertical = 4.dp)
                    .aspectRatio(VerticalImageAspectRatio)
                    .width(TraktTheme.size.verticalMediumMediaCardSize)
                    .clip(RoundedCornerShape(corner - 2.dp))
                    .background(TraktTheme.colors.placeholderContainer)
                    .onClick(onClick = onImageClick ?: {}),
            ) {
                Image(
                    painter = painterResource(R.drawable.ic_placeholder_vertical_border),
                    contentDescription = null,
                    contentScale = ContentScale.Fit,
                    colorFilter = ColorFilter.tint(TraktTheme.colors.placeholderContent),
                    modifier = Modifier.padding(5.dp),
                )
                Icon(
                    painter = painterResource(R.drawable.ic_placeholder_trakt),
                    contentDescription = null,
                    tint = TraktTheme.colors.placeholderContent,
                    modifier = Modifier
                        .size(54.dp)
                        .align(Alignment.TopEnd)
                        .graphicsLayer {
                            translationX = 4.dp.toPx()
                            translationY = -4.dp.toPx()
                        },
                )
                Icon(
                    painter = painterResource(R.drawable.ic_trakt_logo),
                    contentDescription = null,
                    tint = TraktTheme.colors.placeholderContent,
                    modifier = Modifier
                        .size(32.dp)
                        .align(Alignment.Center)
                        .graphicsLayer {
                            translationY = 12.dp.toPx()
                        },
                )
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .fillMaxHeight(0.25f)
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
        }

        Box {
            if (!containerImageUrl.isNullOrBlank() && !isContainerError) {
                val inspection = LocalInspectionMode.current
                val gradientColor2 = when {
                    inspection -> Purple500
                    else -> containerColor.copy(alpha = 0F)
                }

                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(containerImageUrl)
                        .crossfade(true)
                        .build(),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    onError = { isContainerError = true },
                    modifier = Modifier
                        .padding(start = TraktTheme.size.verticalMediumMediaCardSize / 1.25F)
                        .fillMaxSize()
                        .drawWithContent {
                            drawContent()
                            drawRect(
                                brush = linearGradient(
                                    colors = listOf(
                                        containerColor,
                                        gradientColor2,
                                    ),
                                    start = Offset(size.width / 1.75F, size.height),
                                    end = Offset(size.width * 1.655F, -size.height),
                                ),
                                size = size,
                            )
                        },
                )
            }

            Column(
                verticalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .padding(vertical = 10.dp)
                    .padding(start = 12.dp)
                    .padding(end = 12.dp)
                    .fillMaxSize(),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.Top,
                ) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(3.dp),
                        modifier = Modifier.weight(1F),
                    ) {
                        Text(
                            text = title,
                            style = TraktTheme.typography.cardTitle.copy(fontSize = 16.sp),
                            color = TraktTheme.colors.textPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )

                        if (!titleOriginal.isNullOrBlank() && !titleOriginal.equals(title, ignoreCase = true)) {
                            Text(
                                text = "($titleOriginal)",
                                style = TraktTheme.typography.cardSubtitle.copy(fontSize = 12.sp),
                                color = TraktTheme.colors.textSecondary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.padding(bottom = 3.dp),
                            )
                        }

                        Text(
                            text = subtitle,
                            style = TraktTheme.typography.cardSubtitle.copy(fontSize = 12.sp),
                            color = TraktTheme.colors.textSecondary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }

                    if (more) {
                        Icon(
                            painter = painterResource(R.drawable.ic_more_vertical),
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier
                                .padding(top = 2.dp)
                                .graphicsLayer {
                                    translationX = 5.dp.toPx()
                                }
                                .size(14.dp)
                                .onClick(onClick = onLongClick ?: {}),
                        )
                    }
                }

                footerContent?.let { it() }
            }
        }
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
            PanelMediaCard(
                title = "Lorem",
                titleOriginal = null,
                subtitle = "Action, Adventure",
                contentImageUrl = null,
                containerImageUrl = null,
            )
        }
    }
}

@Preview
@Composable
private fun PosterPreviewPlaceholder() {
    TraktTheme {
        PanelMediaCard(
            title = "Lorem",
            titleOriginal = "Original Lorem",
            subtitle = "Action, Adventure",
            contentImageUrl = null,
            containerImageUrl = null,
        )
    }
}

@Preview
@Composable
private fun PosterPreviewChipPlaceholder() {
    TraktTheme {
        PanelMediaCard(
            title = "Lorem",
            titleOriginal = null,
            subtitle = "Action, Adventure",
            contentImageUrl = null,
            containerImageUrl = null,
            footerContent = {
                Row(
                    horizontalArrangement = spacedBy(TraktTheme.spacing.chipsSpace),
                ) {
                    InfoChip(
                        text = "Watched",
                    )
                    InfoChip(
                        text = "PG-18",
                    )
                }
            },
        )
    }
}
