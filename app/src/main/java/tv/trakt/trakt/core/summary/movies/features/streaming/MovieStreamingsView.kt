@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)

package tv.trakt.trakt.core.summary.movies.features.streaming

import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateColor
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.ColorImage
import coil3.annotation.ExperimentalCoilApi
import coil3.compose.AsyncImage
import coil3.compose.AsyncImagePreviewHandler
import coil3.compose.LocalAsyncImagePreviewHandler
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import tv.trakt.trakt.common.helpers.LoadingState.DONE
import tv.trakt.trakt.common.helpers.LoadingState.IDLE
import tv.trakt.trakt.common.helpers.LoadingState.LOADING
import tv.trakt.trakt.common.helpers.extensions.onClick
import tv.trakt.trakt.common.helpers.extensions.openWatchNowLink
import tv.trakt.trakt.common.model.streamings.StreamingService
import tv.trakt.trakt.common.model.streamings.StreamingType
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.components.TraktHeader
import tv.trakt.trakt.ui.theme.TraktTheme

@Composable
internal fun MovieStreamingsView(
    viewModel: MovieStreamingsViewModel,
    headerPadding: PaddingValues,
    contentPadding: PaddingValues,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val uriHandler = LocalUriHandler.current

    val state by viewModel.state.collectAsStateWithLifecycle()

    MovieStreamingsContent(
        state = state,
        modifier = modifier,
        headerPadding = headerPadding,
        contentPadding = contentPadding,
        onClick = { service ->
            openWatchNowLink(
                context = context,
                uriHandler = uriHandler,
                link = service.linkDirect,
            )
        },
    )
}

@Composable
private fun MovieStreamingsContent(
    state: MovieStreamingsState,
    modifier: Modifier = Modifier,
    headerPadding: PaddingValues = PaddingValues(),
    contentPadding: PaddingValues = PaddingValues(),
    onClick: ((StreamingService) -> Unit)? = null,
) {
    Column(
        verticalArrangement = spacedBy(12.dp),
        modifier = modifier,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(headerPadding),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = CenterVertically,
        ) {
            TraktHeader(
                title = stringResource(R.string.page_title_where_to_watch),
            )
        }

        Crossfade(
            targetState = state.loading,
            animationSpec = tween(200),
        ) { loading ->
            when (loading) {
                IDLE, LOADING -> {
                    ContentLoading(
                        visible = loading.isLoading,
                        contentPadding = contentPadding,
                    )
                }
                DONE -> {
                    if (state.items?.isEmpty() == true) {
                        ContentEmpty(
                            contentPadding = headerPadding,
                        )
                    } else {
                        ContentList(
                            listItems = (state.items ?: emptyList()).toImmutableList(),
                            contentPadding = contentPadding,
                            onClick = onClick,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ContentList(
    listItems: ImmutableList<Pair<StreamingService, StreamingType>>,
    listState: LazyListState = rememberLazyListState(),
    contentPadding: PaddingValues,
    onClick: ((StreamingService) -> Unit)? = null,
) {
    LazyRow(
        state = listState,
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = spacedBy(32.dp),
        verticalAlignment = CenterVertically,
        contentPadding = contentPadding,
    ) {
        items(
            items = listItems,
            key = { "${it.second}_${it.first.source}" },
        ) { (service, type) ->
            ContentListItem(
                service = service,
                type = type,
                onClick = onClick,
            )
        }
    }
}

@Composable
private fun ContentListItem(
    service: StreamingService,
    type: StreamingType,
    onClick: ((StreamingService) -> Unit)?,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = spacedBy(4.dp, CenterVertically),
        modifier = Modifier
            .onClick(
                onClick = { onClick?.invoke(service) },
            ),
    ) {
        if (service.logo.isNullOrBlank()) {
            Text(
                text = service.name,
                color = TraktTheme.colors.textPrimary,
                style = TraktTheme.typography.buttonPrimary,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .height(40.dp)
                    .wrapContentHeight(align = Alignment.CenterVertically),
            )
        } else {
            AsyncImage(
                model = "https://${service.logo}",
                contentDescription = null,
                contentScale = ContentScale.FillHeight,
                modifier = Modifier
                    .height(40.dp),
            )
        }

        Text(
            text = stringResource(type.labelRes).uppercase(),
            color = TraktTheme.colors.textSecondary,
            style = TraktTheme.typography.meta,
        )
    }
}

@Composable
private fun ContentListSkeletonItem() {
    val infiniteTransition = rememberInfiniteTransition(label = "infiniteTransition")
    val shimmerTransition by infiniteTransition
        .animateColor(
            initialValue = TraktTheme.colors.skeletonContainer,
            targetValue = TraktTheme.colors.skeletonShimmer,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 1000),
                repeatMode = RepeatMode.Reverse,
            ),
            label = "shimmerTransition",
        )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = spacedBy(4.dp, CenterVertically),
    ) {
        val shape = RoundedCornerShape(100)

        Box(
            modifier = Modifier
                .height(40.dp)
                .width(80.dp)
                .clip(shape)
                .background(shimmerTransition),
        )

        Text(
            text = "STREAM",
            color = Color.Transparent,
            style = TraktTheme.typography.meta,
            modifier = Modifier
                .clip(shape)
                .background(shimmerTransition),
        )
    }
}

@Composable
private fun ContentLoading(
    visible: Boolean = true,
    contentPadding: PaddingValues,
) {
    LazyRow(
        horizontalArrangement = spacedBy(TraktTheme.spacing.mainRowSpace),
        contentPadding = contentPadding,
        userScrollEnabled = false,
        modifier = Modifier
            .fillMaxWidth()
            .alpha(if (visible) 1F else 0F),
    ) {
        items(count = 6) {
            ContentListSkeletonItem()
        }
    }
}

@Composable
private fun ContentEmpty(contentPadding: PaddingValues) {
    Text(
        text = stringResource(R.string.button_text_no_services),
        color = TraktTheme.colors.textSecondary,
        style = TraktTheme.typography.heading6,
        modifier = Modifier.padding(contentPadding),
    )
}

@OptIn(ExperimentalCoilApi::class)
@Preview(
    device = "id:pixel_5",
    showBackground = true,
    backgroundColor = 0xFF131517,
)
@Composable
private fun Preview() {
    TraktTheme {
        val previewHandler = AsyncImagePreviewHandler {
            ColorImage(Color.Blue.toArgb())
        }
        CompositionLocalProvider(LocalAsyncImagePreviewHandler provides previewHandler) {
            MovieStreamingsContent(
                state = MovieStreamingsState(),
            )
        }
    }
}

@OptIn(ExperimentalCoilApi::class)
@Preview(
    device = "id:pixel_5",
    showBackground = true,
    backgroundColor = 0xFF131517,
)
@Composable
private fun Preview2() {
    TraktTheme {
        val previewHandler = AsyncImagePreviewHandler {
            ColorImage(Color.Blue.toArgb())
        }
        CompositionLocalProvider(LocalAsyncImagePreviewHandler provides previewHandler) {
            ContentListItem(
                service = StreamingService(
                    source = "Hello",
                    name = "Hello",
                    logo = null,
                    channel = "Hello",
                    linkDirect = "Hello",
                    uhd = false,
                    color = null,
                    country = "Hello",
                    currency = null,
                    purchasePrice = "Hello",
                    rentPrice = "Hello",
                ),
                type = StreamingType.SUBSCRIPTION,
                onClick = {},
            )
        }
    }
}

@OptIn(ExperimentalCoilApi::class)
@Preview(
    device = "id:pixel_5",
    showBackground = true,
    backgroundColor = 0xFF131517,
)
@Composable
private fun Preview3() {
    TraktTheme {
        val previewHandler = AsyncImagePreviewHandler {
            ColorImage(Color.Blue.toArgb())
        }
        CompositionLocalProvider(LocalAsyncImagePreviewHandler provides previewHandler) {
            ContentListSkeletonItem()
        }
    }
}
