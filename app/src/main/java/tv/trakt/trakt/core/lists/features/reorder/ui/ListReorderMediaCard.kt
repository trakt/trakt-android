package tv.trakt.trakt.core.lists.features.reorder.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import tv.trakt.trakt.common.helpers.extensions.DevicePreview
import tv.trakt.trakt.common.helpers.extensions.ifOrElse
import tv.trakt.trakt.common.helpers.extensions.onClick
import tv.trakt.trakt.common.ui.theme.colors.Purple400
import tv.trakt.trakt.common.ui.theme.colors.Shade940
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.theme.TraktTheme
import tv.trakt.trakt.ui.theme.VerticalImageAspectRatio

@Composable
internal fun ListReorderMediaCard(
    rank: String,
    title: String,
    subtitle: String,
    contentImageUrl: String?,
    containerImageUrl: String?,
    modifier: Modifier = Modifier,
    handleModifier: Modifier = Modifier,
    corner: Dp = 16.dp,
    shadow: Dp = 0.dp,
    containerColor: Color = TraktTheme.colors.panelCardContainer,
    onClick: (() -> Unit)? = null,
) {
    var isPosterError by remember { mutableStateOf(false) }
    var isContainerError by remember { mutableStateOf(false) }

    Row(
        horizontalArrangement = spacedBy(0.dp),
        verticalAlignment = Alignment.Top,
        modifier = modifier
            .ifOrElse(
                condition = onClick != null,
                isTrue = Modifier.onClick(indication = true) { onClick?.invoke() },
            )
            .dropShadow(
                shape = RoundedCornerShape(corner),
                shadow = Shadow(
                    radius = shadow,
                    color = Shade940,
                    spread = 2.dp,
                    alpha = if (shadow > 0.dp) 0.33F else 0F,
                ),
            )
            .graphicsLayer {
                clip = false
            }
            .background(containerColor, RoundedCornerShape(corner))
            .height(TraktTheme.size.verticalTinyMediaCardSize / VerticalImageAspectRatio),
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .width(TraktTheme.size.verticalTinyMediaCardSize),
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
                        .width(TraktTheme.size.verticalTinyMediaCardSize)
                        .clip(RoundedCornerShape(corner - 3.dp)),
                )
            } else {
                Box(
                    modifier = Modifier
                        .padding(start = 4.dp)
                        .padding(vertical = 4.dp)
                        .aspectRatio(VerticalImageAspectRatio)
                        .width(TraktTheme.size.verticalTinyMediaCardSize)
                        .clip(RoundedCornerShape(corner - 3.dp))
                        .background(TraktTheme.colors.placeholderContainer),
                ) {
                    Image(
                        painter = painterResource(R.drawable.ic_placeholder_vertical_border),
                        contentDescription = null,
                        contentScale = ContentScale.Fit,
                        colorFilter = ColorFilter.tint(TraktTheme.colors.placeholderContent),
                        modifier = Modifier.padding(6.dp),
                    )
                    Icon(
                        painter = painterResource(R.drawable.ic_placeholder_trakt),
                        contentDescription = null,
                        tint = TraktTheme.colors.placeholderContent,
                        modifier = Modifier
                            .size(32.dp)
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
                            .size(24.dp)
                            .align(Alignment.Center)
                            .graphicsLayer {
                                translationY = 14.dp.toPx()
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
        }

        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(corner)),
        ) {
            if (!containerImageUrl.isNullOrBlank() && !isContainerError) {
                val inspection = LocalInspectionMode.current
                val gradientColor2 = when {
                    inspection -> TraktTheme.colors.accent
                    else -> containerColor.copy(alpha = 0.55F)
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
                        .padding(start = TraktTheme.size.verticalTinyMediaCardSize / 1.25F)
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
                verticalArrangement = Arrangement.Center,
                modifier = Modifier
                    .padding(vertical = 10.dp)
                    .padding(start = 12.dp)
                    .padding(end = 64.dp)
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
                            text = rank,
                            style = TraktTheme.typography.cardTitle.copy(fontSize = 14.sp),
                            color = Purple400,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )

                        Text(
                            text = title,
                            style = TraktTheme.typography.cardTitle.copy(fontSize = 16.sp),
                            color = TraktTheme.colors.textPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )

                        Text(
                            text = subtitle,
                            style = TraktTheme.typography.cardSubtitle.copy(fontSize = 12.sp),
                            color = TraktTheme.colors.textSecondary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
            }

            Icon(
                painter = painterResource(R.drawable.ic_drag),
                contentDescription = null,
                tint = TraktTheme.colors.textPrimary,
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    // Swallow taps on the handle so they don't reach the card's onClick.
                    .onClick(throttle = false) {}
                    .then(handleModifier)
                    .padding(end = 12.dp)
                    .size(28.dp),
            )
        }
    }
}

@OptIn(ExperimentalCoilApi::class)
@DevicePreview
@Composable
private fun PosterPreview() {
    TraktTheme {
        val previewHandler = AsyncImagePreviewHandler {
            ColorImage(Color.Blue.toArgb())
        }
        CompositionLocalProvider(LocalAsyncImagePreviewHandler provides previewHandler) {
            ListReorderMediaCard(
                rank = "#999",
                title = "Lorem Ipsum",
                subtitle = "2026",
                contentImageUrl = null,
                containerImageUrl = null,
            )
        }
    }
}
