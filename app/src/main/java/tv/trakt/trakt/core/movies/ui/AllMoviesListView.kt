package tv.trakt.trakt.core.movies.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Arrangement.Absolute.spacedBy
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush.Companion.linearGradient
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import tv.trakt.trakt.common.helpers.extensions.durationFormat
import tv.trakt.trakt.common.model.Images.Size.THUMB
import tv.trakt.trakt.common.model.Movie
import tv.trakt.trakt.common.ui.composables.FilmProgressIndicator
import tv.trakt.trakt.common.ui.theme.colors.Purple500
import tv.trakt.trakt.common.ui.theme.colors.Red500
import tv.trakt.trakt.helpers.preview.PreviewData
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.components.InfoChip
import tv.trakt.trakt.ui.theme.TraktTheme
import tv.trakt.trakt.ui.theme.VerticalImageAspectRatio

@OptIn(ExperimentalFoundationApi::class)
@Composable
internal fun AllMoviesListView(
    state: LazyListState,
    items: ImmutableList<Movie>,
    modifier: Modifier = Modifier,
    title: @Composable (() -> Unit)? = null,
    loading: Boolean = false,
    onItemLongClick: (Movie) -> Unit = {},
    onTopOfList: () -> Unit = {},
    onEndOfList: () -> Unit = {},
) {
    val contentPadding = PaddingValues(
        start = TraktTheme.spacing.mainPageHorizontalSpace,
        end = TraktTheme.spacing.mainPageHorizontalSpace,
        top = WindowInsets.statusBars.asPaddingValues()
            .calculateTopPadding(),
        bottom = WindowInsets.navigationBars.asPaddingValues()
            .calculateBottomPadding()
            .plus(TraktTheme.size.navigationBarHeight * 2),
    )

    val isScrolledToBottom by remember(items.size) {
        derivedStateOf {
            state.firstVisibleItemIndex >= (items.size - 5)
        }
    }

    val isScrolledToTop by remember {
        derivedStateOf {
            state.firstVisibleItemIndex == 0 &&
                state.firstVisibleItemScrollOffset == 0
        }
    }

    LaunchedEffect(isScrolledToTop) {
        if (isScrolledToTop) {
            onTopOfList()
        }
    }

    LaunchedEffect(isScrolledToBottom) {
        if (isScrolledToBottom) {
            onEndOfList()
        }
    }

    LazyColumn(
        state = state,
        verticalArrangement = spacedBy(0.dp),
        contentPadding = contentPadding,
        overscrollEffect = null,
        modifier = modifier,
    ) {
        if (title != null) {
            item { title() }
        }

        listItems(
            items = items,
            onLongClick = onItemLongClick,
        )

        if (loading) {
            item {
                FilmProgressIndicator(
                    size = 36.dp,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}

private fun LazyListScope.listItems(
    items: ImmutableList<Movie>,
    onLongClick: ((Movie) -> Unit)? = null,
) {
    items(
        items = items,
        key = { it.ids.trakt.value },
    ) { item ->

        var isPosterError by remember { mutableStateOf(false) }
        var isContainerError by remember { mutableStateOf(false) }

        Row(
            horizontalArrangement = spacedBy(0.dp),
            verticalAlignment = Alignment.Top,
            modifier = Modifier
                .padding(bottom = TraktTheme.spacing.mainListVerticalSpace)
                .clip(RoundedCornerShape(12.dp))
                .background(TraktTheme.colors.listItemContainer)
                .height(TraktTheme.size.verticalMediumMediaCardSize / VerticalImageAspectRatio)
                .combinedClickable(
                    onClick = {},
                    onLongClick = when {
                        onLongClick != null -> {
                            { onLongClick(item) }
                        }
                        else -> null
                    },
                    interactionSource = remember { MutableInteractionSource() },
                    indication = ripple(),
                ),
        ) {
            if (!item.images?.getPosterUrl().isNullOrBlank() && !isPosterError) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(item.images?.getPosterUrl())
                        .crossfade(true)
                        .build(),
                    contentDescription = "Card image",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .aspectRatio(VerticalImageAspectRatio)
                        .width(TraktTheme.size.verticalMediumMediaCardSize)
                        .clip(RoundedCornerShape(12.dp)),
                )
            } else {
                Box(
                    modifier = Modifier
                        .padding(start = 8.dp)
                        .padding(vertical = 8.dp)
                        .aspectRatio(VerticalImageAspectRatio)
                        .width(TraktTheme.size.verticalMediumMediaCardSize),
                ) {
                    Image(
                        painter = painterResource(R.drawable.ic_placeholder_vertical_border),
                        contentDescription = null,
                        contentScale = ContentScale.Fit,
                        colorFilter = ColorFilter.tint(TraktTheme.colors.placeholderContent),
                    )
                    Icon(
                        painter = painterResource(R.drawable.ic_placeholder_trakt),
                        contentDescription = null,
                        tint = TraktTheme.colors.placeholderContent,
                        modifier = Modifier
                            .size(48.dp)
                            .align(Alignment.TopEnd)
                            .graphicsLayer {
                                translationX = -3.dp.toPx()
                                translationY = 1.dp.toPx()
                            },
                    )
                }
            }

            Box {
                if (!item.images?.getFanartUrl().isNullOrBlank() && !isContainerError) {
                    val inspection = LocalInspectionMode.current
                    val gradientColor = TraktTheme.colors.listItemContainer
                    val gradientColor2 = when {
                        inspection -> Purple500
                        else -> gradientColor.copy(alpha = 0F)
                    }

                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(item.images?.getFanartUrl(THUMB))
                            .crossfade(true)
                            .build(),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        onError = { isContainerError = true },
                        modifier = Modifier
                            .padding(start = TraktTheme.size.verticalMediumMediaCardSize)
                            .fillMaxSize()
                            .drawWithContent {
                                drawContent()
                                drawRect(
                                    brush = linearGradient(
                                        colors = listOf(
                                            gradientColor,
                                            gradientColor2,
                                        ),
                                        start = Offset(size.width / 1.66F, size.height),
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
                        .padding(vertical = 12.dp)
                        .padding(start = 12.dp)
                        .padding(end = 16.dp)
                        .fillMaxSize(),
                ) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(0.dp),
                    ) {
                        Text(
                            text = item.title,
                            style = TraktTheme.typography.cardTitle.copy(fontSize = 16.sp),
                            color = TraktTheme.colors.textPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.padding(bottom = 2.dp),
                        )

                        if (!item.titleOriginal.isNullOrBlank() && item.titleOriginal != item.title) {
                            Text(
                                text = "(${item.titleOriginal})",
                                style = TraktTheme.typography.cardTitle.copy(fontSize = 15.sp),
                                color = TraktTheme.colors.textPrimary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.padding(bottom = 2.dp),
                            )
                        }

                        Text(
                            text = item.genres
                                .take(2)
                                .joinToString(", ") {
                                    it.replaceFirstChar { it.uppercaseChar() }
                                },
                            style = TraktTheme.typography.cardSubtitle.copy(fontSize = 13.sp),
                            color = TraktTheme.colors.textSecondary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }

                    Row(
                        horizontalArrangement = spacedBy(TraktTheme.spacing.chipsSpace),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        item.released?.let {
                            InfoChip(
                                text = it.year.toString(),
                                containerColor = TraktTheme.colors.chipContainerOnContent,
                            )
                        }
                        item.runtime?.let {
                            InfoChip(
                                text = it.inWholeMinutes.durationFormat(),
                                containerColor = TraktTheme.colors.chipContainerOnContent,
                            )
                        }
                        if (!item.certification.isNullOrBlank()) {
                            InfoChip(
                                text = item.certification ?: "NR",
                                containerColor = TraktTheme.colors.chipContainerOnContent,
                            )
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = spacedBy(3.dp),
                        ) {
                            val grayFilter = remember {
                                ColorFilter.colorMatrix(
                                    ColorMatrix().apply {
                                        setToSaturation(0F)
                                    },
                                )
                            }
                            val redFilter = remember {
                                ColorFilter.tint(Red500)
                            }

                            Spacer(modifier = Modifier.weight(1F))

                            Image(
                                painter = painterResource(R.drawable.ic_heart),
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                colorFilter = if (item.rating.rating > 0) redFilter else grayFilter,
                            )
                            Text(
                                text = if (item.rating.rating > 0) "${item.rating.ratingPercent}%" else "-",
                                color = TraktTheme.colors.textPrimary,
                                style = TraktTheme.typography.meta,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Preview(
    device = "id:pixel_5",
    showBackground = true,
    backgroundColor = 0xFF131517,
)
@Composable
private fun AllMoviesListViewPreview() {
    TraktTheme {
        AllMoviesListView(
            state = LazyListState(),
            items = listOf(
                PreviewData.movie1,
                PreviewData.movie2,
            ).toImmutableList(),
        )
    }
}
