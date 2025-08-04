package tv.trakt.trakt.app.core.streamings

import androidx.compose.foundation.background
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridScope
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight.Companion.W700
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.tv.material3.Text
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.toImmutableMap
import tv.trakt.trakt.app.R
import tv.trakt.trakt.app.common.model.StreamingService
import tv.trakt.trakt.app.common.ui.FilmProgressIndicator
import tv.trakt.trakt.app.common.ui.GenericErrorView
import tv.trakt.trakt.app.core.details.ui.BackdropImage
import tv.trakt.trakt.app.core.streamings.views.AllStreamingItemView
import tv.trakt.trakt.app.helpers.extensions.openWatchNowLink
import tv.trakt.trakt.app.ui.theme.TraktTheme

private val sections = listOf(
    "initial",
    "content",
)

private val types = mapOf(
    "favorite" to R.string.header_streaming_favorite,
    "free" to R.string.header_streaming_free,
    "subscription" to R.string.header_streaming_subscriptions,
    "purchase" to R.string.header_streaming_purchase,
    "rent" to R.string.header_streaming_rent,
)

@Composable
internal fun AllStreamingsScreen(viewModel: AllStreamingsViewModel) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    AllStreamingsContent(
        state = state,
    )
}

@Composable
internal fun AllStreamingsContent(
    state: AllStreamingsState,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val uriHandler = LocalUriHandler.current

    val focusRequesters = remember {
        sections.associateBy(
            keySelector = { it },
            valueTransform = { FocusRequester() },
        )
    }

    LaunchedEffect(Unit) {
        focusRequesters["initial"]?.requestFocus()
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(TraktTheme.colors.backgroundPrimary),
    ) {
        BackdropImage(
            imageUrl = "",
            blur = 4.dp,
            imageAlpha = 0.85F,
            saturation = 0.95F,
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    top = TraktTheme.spacing.mainContentVerticalSpace,
                    bottom = TraktTheme.spacing.mainContentVerticalSpace,
                    start = TraktTheme.spacing.mainContentStartSpace,
                    end = 24.dp,
                )
                .clip(RoundedCornerShape(16.dp))
                .background(TraktTheme.colors.dialogContainer),
        ) {
            Text(
                text = stringResource(R.string.stream_more_options),
                color = TraktTheme.colors.textPrimary,
                style = TraktTheme.typography.heading4,
                modifier = Modifier
                    .padding(
                        top = 24.dp,
                        start = 32.dp,
                    )
                    .focusRequester(focusRequesters.getValue("initial"))
                    .focusable(),
            )

            when {
                state.loading -> {
                    FilmProgressIndicator(
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally)
                            .padding(top = 16.dp),
                    )
                }
                state.error == null && state.services.isNullOrEmpty() -> {
                    Text(
                        text = stringResource(R.string.stream_no_services),
                        color = TraktTheme.colors.textSecondary,
                        style = TraktTheme.typography.heading6,
                        modifier = Modifier
                            .padding(
                                top = 16.dp,
                                start = 32.dp,
                            ),
                    )
                }
                else -> {
                    AllStreamingsContentGrid(
                        items = (state.services ?: emptyMap()).toImmutableMap(),
                        focusRequesters = focusRequesters,
                        onItemClick = {
                            openWatchNowLink(
                                context = context,
                                uriHandler = uriHandler,
                                link = it.linkDirect,
                            )
                        },
                        modifier = Modifier
                            .padding(top = 8.dp),
                    )
                }
            }

            if (state.error != null) {
                GenericErrorView(
                    error = state.error,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                )
            }
        }
    }
}

@Composable
private fun AllStreamingsContentGrid(
    items: ImmutableMap<String, List<StreamingService>>,
    modifier: Modifier = Modifier,
    focusRequesters: Map<String, FocusRequester>,
    onItemClick: (StreamingService) -> Unit,
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(4),
        horizontalArrangement = spacedBy(12.dp),
        verticalArrangement = spacedBy(12.dp),
        contentPadding = PaddingValues(
            start = 32.dp,
            end = 32.dp,
            bottom = 32.dp,
        ),
        modifier = modifier.focusRequester(
            focusRequesters.getValue("content"),
        ),
    ) {
        types.forEach { (type, headerRes) ->
            streamingsGridSection(
                headerRes = headerRes,
                type = type,
                items = items[type] ?: emptyList(),
                onItemClick = onItemClick,
            )
        }
    }
}

private fun LazyGridScope.streamingsGridSection(
    headerRes: Int,
    type: String,
    items: List<StreamingService>,
    onItemClick: (StreamingService) -> Unit,
) {
    if (items.isEmpty()) {
        return
    }

    item(span = { GridItemSpan(maxLineSpan) }) {
        Text(
            text = stringResource(headerRes).uppercase(),
            color = TraktTheme.colors.textSecondary,
            style = TraktTheme.typography.paragraphSmall.copy(fontWeight = W700),
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(top = 12.dp),
        )
    }

    items(
        count = items.size,
        key = {
            val item = items[it]
            "${item.source}-${item.country}-$type"
        },
    ) { index ->
        val item = items.getOrNull(index) ?: return@items

        val currencySymbol = item.currency?.symbol
        val currencySpace = remember(currencySymbol) {
            if (currencySymbol?.count() == 1) "" else " "
        }

        AllStreamingItemView(
            name = item.name,
            country = item.country,
            logo = item.logo,
            price = when (type) {
                "purchase" -> remember(item.purchasePrice) {
                    "$currencySymbol$currencySpace${item.purchasePrice}".trim()
                }
                "rent" -> remember(item.rentPrice) {
                    "$currencySymbol$currencySpace${item.rentPrice}".trim()
                }
                else -> null
            },
            contentColor = item.color ?: Color.Black,
            onClick = { onItemClick(item) },
        )
    }
}

@Preview(
    device = "id:tv_4k",
    showBackground = true,
    backgroundColor = 0xFF131517,
)
@Composable
private fun Preview() {
    TraktTheme {
        AllStreamingsContent(
            state = AllStreamingsState(),
        )
    }
}
