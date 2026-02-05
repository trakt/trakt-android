@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalCoilApi::class)

package tv.trakt.trakt.core.checkin.ui

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Arrangement.Absolute.spacedBy
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.dropShadow
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.shadow.Shadow
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight.Companion.W400
import androidx.compose.ui.text.font.FontWeight.Companion.W500
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow.Companion.Ellipsis
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
import kotlinx.coroutines.delay
import tv.trakt.trakt.common.helpers.extensions.durationFormat
import tv.trakt.trakt.common.helpers.extensions.nowUtcInstant
import tv.trakt.trakt.common.helpers.extensions.onClick
import tv.trakt.trakt.common.ui.theme.colors.Red400
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.components.confirmation.ConfirmationSheet
import tv.trakt.trakt.ui.theme.HorizontalCheckInImageAspectRatio
import tv.trakt.trakt.ui.theme.TraktTheme
import java.time.Instant
import kotlin.time.Duration.Companion.minutes

private val viewShape = RoundedCornerShape(20.dp)
private val viewPadding = 7.dp
private val imageShape = RoundedCornerShape(14.dp)
private val imageHeight = 76.dp
private val imageShadow = 3.dp

private val collapsedViewShape = RoundedCornerShape(16.dp)
private val collapsedViewPadding = 6.dp
private val collapsedImageShape = RoundedCornerShape(11.dp)
private val collapsedImageHeight = 38.dp
private val collapsedImageShadow = 2.dp

@Composable
internal fun CheckInView(
    modifier: Modifier = Modifier,
    image: String? = null,
    title: String? = null,
    subtitle: String? = null,
    startedAt: Instant?,
    expiresAt: Instant?,
    onCloseClick: () -> Unit = {},
) {
    var expanded by rememberSaveable { mutableStateOf(true) }
    var confirmClose by remember { mutableStateOf(false) }

    var timestamp by remember { mutableStateOf(nowUtcInstant()) }

    val secondsLeft = remember(timestamp) {
        expiresAt?.let {
            val duration = it.epochSecond - timestamp.epochSecond
            duration.coerceAtLeast(0L)
        } ?: 0L
    }
    val minutesLeft = remember(secondsLeft) {
        secondsLeft / 60L
    }

    val totalSeconds = remember(startedAt, expiresAt) {
        if (startedAt != null && expiresAt != null) {
            val duration = expiresAt.epochSecond - startedAt.epochSecond
            duration.coerceAtLeast(0L)
        } else {
            0L
        }
    }

    LaunchedEffect(Unit) {
        while (true) {
            delay(1000L)
            timestamp = nowUtcInstant()
        }
    }

    Box(
        modifier = modifier
            .dropShadow(
                shape = viewShape,
                shadow = Shadow(
                    radius = 4.dp,
                    color = Color.Black,
                    spread = 2.dp,
                    alpha = 0.15F,
                ),
            )
            .background(
                color = TraktTheme.colors.navigationContainer,
                shape = when {
                    expanded -> viewShape
                    else -> collapsedViewShape
                },
            )
            .onClick(onClick = {})
            .animateContentSize(),
    ) {
        if (expanded) {
            ExpandedView(
                image = image,
                title = title,
                subtitle = subtitle,
                totalDurationSeconds = { totalSeconds },
                durationSeconds = { secondsLeft },
                durationMinutes = { minutesLeft },
                onCollapseClick = { expanded = false },
                onCloseClick = { confirmClose = true },
                modifier = Modifier.padding(viewPadding),
            )
        } else {
            CollapsedView(
                image = image,
                title = title,
                subtitle = subtitle,
                totalDurationSeconds = { totalSeconds },
                durationSeconds = { secondsLeft },
                onExpandClick = { expanded = true },
                onCloseClick = { confirmClose = true },
                modifier = Modifier.padding(collapsedViewPadding),
            )
        }
    }

    ConfirmationSheet(
        active = confirmClose,
        onYes = {
            confirmClose = false
            onCloseClick()
        },
        onNo = {
            confirmClose = false
        },
        title = stringResource(R.string.text_now_watching),
        message = stringResource(
            R.string.warning_prompt_stop_watching,
            title ?: "",
        ),
        yesColor = Red400,
    )
}

@Composable
private fun ExpandedView(
    image: String?,
    title: String?,
    subtitle: String?,
    totalDurationSeconds: () -> Long,
    durationSeconds: () -> Long,
    durationMinutes: () -> Long,
    modifier: Modifier = Modifier,
    onCollapseClick: () -> Unit = {},
    onCloseClick: () -> Unit = {},
) {
    Box(
        modifier = modifier,
    ) {
        Row(
            horizontalArrangement = spacedBy(10.dp),
        ) {
            ImageView(
                image = image,
                height = imageHeight,
                shape = imageShape,
                shadow = imageShadow,
            )

            Column(
                verticalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .weight(1F)
                    .height(imageHeight)
                    .padding(vertical = 2.dp),
            ) {
                Text(
                    text = stringResource(R.string.text_now_watching),
                    color = TraktTheme.colors.textPrimary,
                    style = TraktTheme.typography.cardTitle.copy(
                        fontWeight = W400,
                        fontSize = 11.sp,
                    ),
                )

                Column(
                    verticalArrangement = spacedBy(6.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(end = 8.dp),
                ) {
                    Row(
                        verticalAlignment = Alignment.Bottom,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Column(
                            verticalArrangement = spacedBy(2.dp, Alignment.Bottom),
                            modifier = Modifier.weight(1F, fill = false),
                        ) {
                            title?.let {
                                Text(
                                    text = title,
                                    color = TraktTheme.colors.textPrimary,
                                    style = TraktTheme.typography.cardTitle.copy(
                                        fontWeight = W500,
                                    ),
                                    maxLines = 1,
                                    overflow = Ellipsis,
                                )
                            }
                            subtitle?.let {
                                Text(
                                    text = subtitle,
                                    color = TraktTheme.colors.textSecondary,
                                    style = TraktTheme.typography.cardSubtitle.copy(
                                        fontSize = 11.sp,
                                    ),
                                    maxLines = 1,
                                    overflow = Ellipsis,
                                )
                            }
                        }

                        val minutes = durationMinutes()
                        val durationText = when {
                            minutes >= 1L -> minutes.durationFormat()
                            else -> "<${1L.durationFormat()}"
                        }
                        Text(
                            text = stringResource(R.string.text_time_remaining, durationText),
                            textAlign = TextAlign.End,
                            color = TraktTheme.colors.textSecondary,
                            style = TraktTheme.typography.cardSubtitle.copy(
                                fontSize = 10.sp,
                            ),
                            maxLines = 2,
                            overflow = Ellipsis,
                            modifier = Modifier
                                .padding(start = 16.dp, end = 0.5.dp),
                        )
                    }

                    LinearProgressIndicator(
                        progress = {
                            val total = totalDurationSeconds().toFloat()
                            val current = durationSeconds().toFloat()
                            when {
                                total > 0F -> (total - current) / total
                                else -> 0F
                            }
                        },
                        color = Color.White,
                        trackColor = Color.White.copy(alpha = 0.2F),
                        gapSize = 3.dp,
                        drawStopIndicator = { },
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 2.dp, end = 4.dp),
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_cheveron_down),
                contentDescription = null,
                tint = TraktTheme.colors.textPrimary,
                modifier = Modifier
                    .size(18.dp)
                    .onClick(onClick = onCollapseClick),
            )

            Icon(
                painter = painterResource(R.drawable.ic_close_2),
                contentDescription = null,
                tint = TraktTheme.colors.textPrimary,
                modifier = Modifier
                    .size(18.dp)
                    .onClick(onClick = onCloseClick),
            )
        }
    }
}

@Composable
private fun CollapsedView(
    image: String?,
    title: String?,
    subtitle: String?,
    totalDurationSeconds: () -> Long,
    durationSeconds: () -> Long,
    modifier: Modifier = Modifier,
    onExpandClick: () -> Unit = {},
    onCloseClick: () -> Unit = {},
) {
    Box(
        modifier = modifier,
    ) {
        Row(
            horizontalArrangement = spacedBy(7.dp),
        ) {
            ImageView(
                image = image,
                height = collapsedImageHeight,
                shape = collapsedImageShape,
                shadow = collapsedImageShadow,
            )

            Column(
                verticalArrangement = spacedBy(5.dp, Alignment.CenterVertically),
                modifier = Modifier
                    .weight(1F)
                    .height(collapsedImageHeight),
            ) {
                Row(
                    verticalAlignment = Alignment.Bottom,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(end = 4.dp),
                ) {
                    Column(
                        verticalArrangement = spacedBy(0.dp, Alignment.Bottom),
                        modifier = Modifier
                            .weight(1F, fill = false),
                    ) {
                        title?.let {
                            Text(
                                text = title,
                                color = TraktTheme.colors.textPrimary,
                                style = TraktTheme.typography.cardTitle.copy(
                                    fontWeight = W500,
                                ),
                                maxLines = 1,
                                overflow = Ellipsis,
                            )
                        }
                        subtitle?.let {
                            Text(
                                text = subtitle,
                                color = TraktTheme.colors.textSecondary,
                                style = TraktTheme.typography.cardSubtitle.copy(
                                    fontSize = 11.sp,
                                ),
                                maxLines = 1,
                                overflow = Ellipsis,
                            )
                        }
                    }
                }

                LinearProgressIndicator(
                    progress = {
                        val total = totalDurationSeconds().toFloat()
                        val current = durationSeconds().toFloat()
                        when {
                            total > 0F -> (total - current) / total
                            else -> 0F
                        }
                    },
                    color = Color.White,
                    trackColor = Color.White.copy(alpha = 0.2F),
                    gapSize = 3.dp,
                    drawStopIndicator = { },
                    modifier = Modifier
                        .height(2.dp)
                        .fillMaxWidth()
                        .padding(end = 8.dp),
                )
            }
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(bottom = 10.dp)
                .padding(end = 5.dp),
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_cheveron_down),
                contentDescription = null,
                tint = TraktTheme.colors.textPrimary,
                modifier = Modifier
                    .size(18.dp)
                    .rotate(180F)
                    .onClick(onClick = onExpandClick),
            )

            Icon(
                painter = painterResource(R.drawable.ic_close_2),
                contentDescription = null,
                tint = TraktTheme.colors.textPrimary,
                modifier = Modifier
                    .size(18.dp)
                    .onClick(onClick = onCloseClick),
            )
        }
    }
}

@Composable
private fun ImageView(
    image: String?,
    height: Dp,
    shape: Shape,
    shadow: Dp,
) {
    var isError by rememberSaveable(image) { mutableStateOf(false) }

    if (!image.isNullOrBlank() && !isError) {
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(image)
                .crossfade(true)
                .build(),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            onError = { isError = true },
            modifier = Modifier
                .height(height)
                .aspectRatio(HorizontalCheckInImageAspectRatio)
                .shadow(shadow, shape)
                .clip(shape)
                .background(color = TraktTheme.colors.placeholderContainer),
        )
    } else {
        ImageViewPlaceholder(
            height = height,
            shape = shape,
            shadow = shadow,
        )
    }
}

@Composable
private fun ImageViewPlaceholder(
    height: Dp,
    shape: Shape,
    shadow: Dp,
) {
    Box(
        modifier = Modifier
            .height(height)
            .aspectRatio(HorizontalCheckInImageAspectRatio)
            .shadow(shadow, shape)
            .clip(shape)
            .background(color = TraktTheme.colors.placeholderContainer),
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_trakt_logo),
            contentDescription = null,
            tint = TraktTheme.colors.placeholderContent,
            modifier = Modifier
                .size(remember(height) { height / 1.3F })
                .align(Alignment.Center),
        )
    }
}

@Preview(
    name = "Expanded",
    device = "id:pixel_5",
    showBackground = false,
)
@Composable
private fun Preview() {
    TraktTheme {
        val previewHandler = AsyncImagePreviewHandler {
            ColorImage(Color.Blue.toArgb())
        }
        CompositionLocalProvider(LocalAsyncImagePreviewHandler provides previewHandler) {
            Box(
                modifier = Modifier.padding(16.dp),
            ) {
                Box(
                    modifier = Modifier
                        .shadow(4.dp, viewShape)
                        .background(
                            color = TraktTheme.colors.navigationContainer,
                            shape = viewShape,
                        ),
                ) {
                    val minutesTotal = 90
                    val minutesLeft = minutesTotal * 0.25
                    ExpandedView(
                        image = "",
                        title = "Stranger Things",
                        subtitle = "Season 2 - Episode 5",
                        totalDurationSeconds = { minutesTotal.minutes.inWholeSeconds },
                        durationSeconds = { minutesLeft.minutes.inWholeSeconds },
                        durationMinutes = { minutesLeft.minutes.inWholeMinutes },
                        modifier = Modifier.padding(viewPadding),
                    )
                }
            }
        }
    }
}

@Preview(
    name = "Collapsed",
    device = "id:pixel_5",
    showBackground = false,
)
@Composable
private fun Preview2() {
    TraktTheme {
        val previewHandler = AsyncImagePreviewHandler {
            ColorImage(Color.Blue.toArgb())
        }
        CompositionLocalProvider(LocalAsyncImagePreviewHandler provides previewHandler) {
            Box(
                modifier = Modifier.padding(16.dp),
            ) {
                Box(
                    modifier = Modifier
                        .shadow(4.dp, collapsedViewShape)
                        .background(
                            color = TraktTheme.colors.navigationContainer,
                            shape = collapsedViewShape,
                        ),
                ) {
                    val minutesTotal = 90
                    val minutesLeft = minutesTotal * 0.25
                    CollapsedView(
                        image = "",
                        title = "Rental Family",
                        subtitle = "Movie",
                        totalDurationSeconds = { minutesTotal.minutes.inWholeSeconds },
                        durationSeconds = { minutesLeft.minutes.inWholeSeconds },
                        modifier = Modifier.padding(collapsedViewPadding),
                    )
                }
            }
        }
    }
}
