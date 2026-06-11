@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalCoilApi::class)

package tv.trakt.trakt.core.ratings.rateprompt.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.dropShadow
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
import androidx.compose.ui.text.style.TextOverflow.Companion.Ellipsis
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.ColorImage
import coil3.annotation.ExperimentalCoilApi
import coil3.compose.AsyncImage
import coil3.compose.AsyncImagePreviewHandler
import coil3.compose.LocalAsyncImagePreviewHandler
import coil3.request.ImageRequest
import coil3.request.crossfade
import org.koin.androidx.compose.koinViewModel
import tv.trakt.trakt.common.helpers.extensions.onClick
import tv.trakt.trakt.common.helpers.extensions.onEmptyClick
import tv.trakt.trakt.common.model.Images.Size
import tv.trakt.trakt.core.ratings.rateprompt.model.RatePromptMedia
import tv.trakt.trakt.core.ratings.ui.UserRatingBar
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.theme.HorizontalCheckInImageAspectRatio
import tv.trakt.trakt.ui.theme.TraktTheme
import java.time.Instant

private val viewShape = RoundedCornerShape(20.dp)
private val viewPadding = 7.dp
private val imageShape = RoundedCornerShape(14.dp)
private val imageHeight = 76.dp
private val imageShadow = 3.dp

@Composable
internal fun RatePromptView(
    modifier: Modifier = Modifier,
    viewModel: RatePromptViewModel = koinViewModel(),
    media: RatePromptMedia,
    onMediaClick: () -> Unit,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    Box(
        modifier = modifier
            .dropShadow(
                shape = viewShape,
                shadow = remember {
                    Shadow(
                        radius = 4.dp,
                        color = Color.Black,
                        spread = 2.dp,
                        alpha = 0.15F,
                    )
                },
            )
            .background(
                color = TraktTheme.colors.navigationContainer,
                shape = viewShape,
            )
            .onEmptyClick(),
    ) {
        ExpandedView(
            key = media.movie.ids.trakt.toString(),
            image = media.movie.images?.getFanartUrl(Size.THUMB),
            title = media.movie.title,
            subtitle = stringResource(
                remember(media.movie.ids.trakt) {
                    listOf(
                        R.string.text_rate_prompt_1,
                        R.string.text_rate_prompt_2,
                        R.string.text_rate_prompt_3,
                    ).random()
                },
            ),
            rating = state.rating,
            favorite = state.favorite,
            favoriteLoading = state.loading.isLoading,
            dismissing = state.dismissing,
            onMediaClick = onMediaClick,
            onCloseClick = {
                viewModel.dismiss()
            },
            onRate = { newRating ->
                when (newRating == null) {
                    true -> viewModel.removeRating()
                    false -> viewModel.addRating(newRating)
                }
            },
            onFavoriteClick = {
                when (state.favorite) {
                    false -> viewModel.addToFavorites()
                    true -> viewModel.removeFromFavorites()
                }
            },
            modifier = Modifier.padding(viewPadding),
        )
    }
}

@Composable
private fun ExpandedView(
    key: String?,
    image: String?,
    title: String?,
    subtitle: String?,
    rating: Int?,
    favorite: Boolean,
    favoriteLoading: Boolean,
    dismissing: Instant?,
    modifier: Modifier = Modifier,
    onRate: (Int?) -> Unit = {},
    onFavoriteClick: () -> Unit = {},
    onMediaClick: () -> Unit = {},
    onCloseClick: () -> Unit = {},
) {
    var isDraggingRate by remember { mutableStateOf(false) }
    val ratingAlphaMask: Float by animateFloatAsState(
        targetValue = if (isDraggingRate) 0.05F else 1F,
        animationSpec = tween(150),
        label = "alpha",
    )

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
                modifier = Modifier.onClick(onClick = onMediaClick),
            )

            Column(
                verticalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .weight(1F)
                    .height(imageHeight)
                    .padding(vertical = 2.dp),
            ) {
                Column(
                    verticalArrangement = spacedBy(2.dp, Alignment.Bottom),
                    modifier = Modifier
                        .weight(1F, fill = false)
                        .padding(end = 42.dp)
                        .alpha(ratingAlphaMask)
                        .onClick(onClick = onMediaClick),
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

                Column(
                    verticalArrangement = spacedBy(6.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(end = 5.dp),
                ) {
                    Row(
                        verticalAlignment = Alignment.Bottom,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text(
                            text = "Rate:",
                            color = TraktTheme.colors.textPrimary,
                            style = TraktTheme.typography.cardTitle.copy(
                                fontWeight = W400,
                                fontSize = 11.sp,
                            ),
                            modifier = Modifier
                                .alpha(ratingAlphaMask),
                        )

                        UserRatingBar(
                            key = key,
                            size = 22.dp,
                            spacing = 7.dp,
                            textSpacing = 42.dp,
                            rating = rating,
                            favorite = favorite,
                            favoriteLoading = favoriteLoading,
                            onRatingDrag = { isDraggingRate = it },
                            onRatingClick = { onRate(it) },
                            onRatingRemoveClick = { onRate(null) },
                            onFavoriteClick = onFavoriteClick,
                        )
                    }
                }
            }
        }

        val animatedProgress = remember { Animatable(0F) }
        LaunchedEffect(dismissing, isDraggingRate, favoriteLoading) {
            if (dismissing != null && !isDraggingRate && !favoriteLoading) {
                animatedProgress.snapTo(0F)
                animatedProgress.animateTo(
                    targetValue = 1F,
                    animationSpec = tween(5000, easing = LinearEasing),
                )
                onCloseClick()
            } else {
                animatedProgress.snapTo(0F)
            }
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier
                .align(Alignment.TopEnd)
                .alpha(ratingAlphaMask)
                .padding(top = 2.dp, end = 4.dp),
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(24.dp),
            ) {
                CircularProgressIndicator(
                    progress = { animatedProgress.value },
                    modifier = Modifier.size(24.dp),
                    trackColor = Color.Transparent,
                    color = TraktTheme.colors.textPrimary,
                    strokeWidth = 1.5.dp,
                )
                Icon(
                    painter = painterResource(R.drawable.ic_close_2),
                    contentDescription = null,
                    tint = TraktTheme.colors.textPrimary,
                    modifier = Modifier
                        .size(21.dp)
                        .onClick(onClick = onCloseClick),
                )
            }
        }
    }
}

@Composable
private fun ImageView(
    image: String?,
    height: Dp,
    shape: Shape,
    shadow: Dp,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    var isError by rememberSaveable(image) { mutableStateOf(false) }

    if (!image.isNullOrBlank() && !isError) {
        val imageRequest = remember(image) {
            ImageRequest.Builder(context)
                .data(image)
                .crossfade(true)
                .build()
        }
        AsyncImage(
            model = imageRequest,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            onError = { isError = true },
            modifier = modifier
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
            modifier = modifier,
        )
    }
}

@Composable
private fun ImageViewPlaceholder(
    height: Dp,
    shape: Shape,
    shadow: Dp,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
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
    locale = "en",
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
                    ExpandedView(
                        key = null,
                        image = "",
                        title = "Lord of the Rings: The Fellowship of the Ring",
                        rating = 7,
                        favorite = true,
                        favoriteLoading = false,
                        dismissing = null,
                        subtitle = "How did you like it?",
                        modifier = Modifier.padding(viewPadding),
                    )
                }
            }
        }
    }
}
