package tv.trakt.trakt.core.streamings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.collections.immutable.toImmutableList
import org.koin.androidx.compose.koinViewModel
import tv.trakt.trakt.common.core.streamings.model.AllStreamingsSection
import tv.trakt.trakt.common.core.streamings.model.StreamingServiceRow
import tv.trakt.trakt.common.helpers.LoadingState.Done
import tv.trakt.trakt.common.helpers.LoadingState.Loading
import tv.trakt.trakt.common.helpers.extensions.DevicePreview
import tv.trakt.trakt.common.helpers.extensions.onClick
import tv.trakt.trakt.common.helpers.extensions.openExternalAppLink
import tv.trakt.trakt.common.helpers.streamingservices.StreamingServiceApp
import tv.trakt.trakt.common.model.streamings.StreamingService
import tv.trakt.trakt.common.model.streamings.StreamingType.FAVORITE
import tv.trakt.trakt.common.model.streamings.StreamingType.SUBSCRIPTION
import tv.trakt.trakt.common.ui.composables.FilmProgressIndicator
import tv.trakt.trakt.core.streamings.ui.AllStreamingsSkeletonRow
import tv.trakt.trakt.core.streamings.ui.AllStreamingsSourceRow
import tv.trakt.trakt.helpers.SimpleScrollConnection
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.components.ScrollableBackdropImage
import tv.trakt.trakt.ui.components.TraktHeader
import tv.trakt.trakt.ui.theme.TraktTheme

@Composable
internal fun AllStreamingsScreen(
    modifier: Modifier = Modifier,
    viewModel: AllStreamingsViewModel = koinViewModel(),
    onNavigateBack: () -> Unit,
) {
    val context = LocalContext.current
    val state by viewModel.state.collectAsStateWithLifecycle()

    AllStreamingsContent(
        state = state,
        modifier = modifier,
        onBackClick = onNavigateBack,
        onServiceClick = { service ->
            openExternalAppLink(
                packageId = StreamingServiceApp.findFromSource(service.source)?.packageId,
                packageName = service.source,
                uri = service.linkDirect?.toUri(),
                context = context,
            )
        },
    )
}

@Composable
private fun AllStreamingsContent(
    state: AllStreamingsState,
    modifier: Modifier = Modifier,
    onBackClick: () -> Unit = {},
    onServiceClick: (StreamingService) -> Unit = {},
) {
    val listState = rememberLazyListState()
    val listScrollConnection = rememberSaveable(saver = SimpleScrollConnection.Saver) {
        SimpleScrollConnection()
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(TraktTheme.colors.backgroundPrimary)
            .nestedScroll(listScrollConnection),
    ) {
        ScrollableBackdropImage(
            imageUrl = state.media.background,
            translation = listScrollConnection.resultOffset,
        )

        val horizontalPadding = TraktTheme.spacing.mainPageHorizontalSpace

        ContentList(
            state = state,
            listState = listState,
            horizontalPadding = horizontalPadding,
            contentPadding = PaddingValues(
                top = WindowInsets.statusBars.asPaddingValues()
                    .calculateTopPadding(),
                bottom = WindowInsets.navigationBars.asPaddingValues()
                    .calculateBottomPadding()
                    .plus(TraktTheme.spacing.mainPageBottomSpace),
            ),
            onBackClick = onBackClick,
            onServiceClick = onServiceClick,
        )
    }
}

@Composable
private fun ContentList(
    state: AllStreamingsState,
    listState: LazyListState,
    horizontalPadding: Dp,
    contentPadding: PaddingValues,
    onBackClick: () -> Unit,
    onServiceClick: (StreamingService) -> Unit,
) {
    LazyColumn(
        state = listState,
        verticalArrangement = spacedBy(TraktTheme.spacing.mainRowSpace),
        contentPadding = contentPadding,
        overscrollEffect = null,
    ) {
        item(key = "title") {
            TitleBar(
                subtitle = state.media.title,
                loading = state.loading == Loading && state.sections.isNotEmpty(),
                modifier = Modifier
                    .padding(horizontal = horizontalPadding)
                    .onClick(onClick = onBackClick),
            )
        }

        state.sections.forEachIndexed { index, section ->
            item(key = "header-${section.type.type}") {
                TraktHeader(
                    title = stringResource(section.type.labelRes),
                    modifier = Modifier.padding(
                        top = when (index) {
                            0 -> 0.dp
                            else -> TraktTheme.spacing.mainRowHeaderSpace
                        },
                        start = horizontalPadding,
                        end = horizontalPadding,
                    ),
                )
            }

            items(
                items = section.rows,
                key = { "${section.type.type}-${it.source}" },
            ) { row ->
                AllStreamingsSourceRow(
                    row = row,
                    type = section.type,
                    startPadding = horizontalPadding,
                    endPadding = horizontalPadding,
                    onServiceClick = onServiceClick,
                )
            }
        }

        if (state.loading != Done && state.sections.isEmpty()) {
            item(key = "skeleton-header") {
                TraktHeader(
                    title = "",
                    modifier = Modifier
                        .padding(
                            start = horizontalPadding,
                            end = horizontalPadding,
                        )
                        .fillMaxWidth(0.4F)
                        .background(
                            color = TraktTheme.colors.skeletonShimmer,
                            shape = RoundedCornerShape(100),
                        ),
                )
            }
            items(count = 1, key = { "skeleton-$it" }) {
                AllStreamingsSkeletonRow(
                    modifier = Modifier.padding(start = horizontalPadding),
                )
            }
        }

        if (state.loading == Done && state.sections.isEmpty()) {
            item(key = "empty") {
                ContentEmptyView(
                    modifier = Modifier.padding(horizontal = horizontalPadding),
                )
            }
        }
    }
}

@Composable
private fun TitleBar(
    subtitle: String?,
    loading: Boolean,
    modifier: Modifier = Modifier,
) {
    Row(
        verticalAlignment = CenterVertically,
        horizontalArrangement = spacedBy(12.dp),
        modifier = modifier
            .fillMaxWidth()
            .height(TraktTheme.size.titleBarHeight)
            .graphicsLayer {
                translationX = -2.dp.toPx()
            },
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_back_arrow),
            contentDescription = null,
            tint = TraktTheme.colors.textPrimary,
        )

        TraktHeader(
            title = stringResource(R.string.page_title_where_to_watch),
            subtitle = subtitle,
        )

        if (loading) {
            Spacer(modifier = Modifier.weight(1F))
            FilmProgressIndicator(size = 16.dp)
        }
    }
}

@Composable
private fun ContentEmptyView(modifier: Modifier = Modifier) {
    Text(
        text = stringResource(R.string.button_text_no_services),
        color = TraktTheme.colors.textSecondary,
        style = TraktTheme.typography.heading6,
        modifier = modifier,
    )
}

// -- Previews --

@DevicePreview
@Composable
private fun Preview() {
    TraktTheme {
        AllStreamingsContent(
            state = AllStreamingsState(
                loading = Loading,
                media = AllStreamingsState.Media(title = "Severance"),
            ),
        )
    }
}

@DevicePreview
@Composable
private fun Preview2() {
    TraktTheme {
        AllStreamingsContent(
            state = AllStreamingsState(
                loading = Done,
                media = AllStreamingsState.Media(title = "Severance"),
                sections = listOf(
                    AllStreamingsSection(
                        type = FAVORITE,
                        rows = listOf(
                            StreamingServiceRow(
                                source = "apple_tv_plus",
                                services = listOf("pl")
                                    .map { previewService(it) }
                                    .toImmutableList(),
                            ),
                        ).toImmutableList(),
                    ),
                    AllStreamingsSection(
                        type = SUBSCRIPTION,
                        rows = listOf(
                            StreamingServiceRow(
                                source = "apple_tv_plus",
                                services = listOf("pl", "us", "gb")
                                    .map { previewService(it) }
                                    .toImmutableList(),
                            ),
                        ).toImmutableList(),
                    ),
                ).toImmutableList(),
            ),
        )
    }
}

private fun previewService(country: String) =
    StreamingService(
        source = "apple_tv_plus",
        name = "Apple TV+",
        logo = null,
        channel = null,
        linkDirect = "https://trakt.tv",
        uhd = false,
        color = null,
        country = country,
        currency = null,
        purchasePrice = null,
        rentPrice = null,
    )
