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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.clearText
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.collections.immutable.ImmutableList
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
import tv.trakt.trakt.common.model.streamings.StreamingType.Favorite
import tv.trakt.trakt.common.model.streamings.StreamingType.Subscription
import tv.trakt.trakt.common.ui.composables.FilmProgressIndicator
import tv.trakt.trakt.core.streamings.ui.AllStreamingsServiceGroup
import tv.trakt.trakt.core.streamings.ui.AllStreamingsSkeletonRow
import tv.trakt.trakt.core.streamings.ui.countryName
import tv.trakt.trakt.helpers.SimpleScrollConnection
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.components.InputField
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
    val searchState = rememberTextFieldState()
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
            searchState = searchState,
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
    searchState: TextFieldState,
    horizontalPadding: Dp,
    contentPadding: PaddingValues,
    onBackClick: () -> Unit,
    onServiceClick: (StreamingService) -> Unit,
) {
    var expandedGroups by rememberSaveable { mutableStateOf(setOf<String>()) }

    val query = searchState.text.toString().trim()
    val sections = remember(state.sections, query) { state.sections.filterByQuery(query) }

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

        if (state.sections.isNotEmpty()) {
            item(key = "search") {
                InputField(
                    state = searchState,
                    border = 1.dp,
                    icon = painterResource(R.drawable.ic_search_off),
                    containerColor = Color.Transparent,
                    placeholder = stringResource(R.string.input_placeholder_search_streaming_services),
                    endSlot = {
                        if (searchState.text.isNotBlank()) {
                            Icon(
                                painter = painterResource(R.drawable.ic_close),
                                contentDescription = null,
                                tint = TraktTheme.colors.textSecondary,
                                modifier = Modifier
                                    .size(18.dp)
                                    .onClick {
                                        searchState.clearText()
                                    },
                            )
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = horizontalPadding)
                        .padding(bottom = 12.dp),
                )
            }
        }

        sections.forEachIndexed { index, section ->
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
                val groupKey = "${section.type.type}-${row.source}"
                AllStreamingsServiceGroup(
                    row = row,
                    type = section.type,
                    expanded = query.isNotBlank() || groupKey in expandedGroups,
                    onToggleExpand = {
                        if (query.isBlank()) {
                            expandedGroups = when (groupKey) {
                                in expandedGroups -> expandedGroups - groupKey
                                else -> expandedGroups + groupKey
                            }
                        }
                    },
                    onServiceClick = onServiceClick,
                    modifier = Modifier
                        .padding(horizontal = horizontalPadding)
                        .animateItem(
                            fadeInSpec = null,
                            fadeOutSpec = null,
                        ),
                )
            }
        }

        if (state.loading != Done && state.sections.isEmpty()) {
            item(key = "skeleton-search") {
                Box(
                    modifier = Modifier
                        .padding(horizontal = horizontalPadding)
                        .padding(bottom = 12.dp)
                        .fillMaxWidth()
                        .height(48.dp)
                        .background(
                            color = TraktTheme.colors.skeletonContainer,
                            shape = RoundedCornerShape(16.dp),
                        )
                        .animateItem(
                            fadeInSpec = null,
                            fadeOutSpec = null,
                        ),
                )
            }

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
                        )
                        .animateItem(
                            fadeInSpec = null,
                            fadeOutSpec = null,
                        ),
                )
            }
            items(count = 1, key = { "skeleton-$it" }) {
                AllStreamingsSkeletonRow(
                    modifier = Modifier
                        .padding(horizontal = horizontalPadding)
                        .animateItem(
                            fadeInSpec = null,
                            fadeOutSpec = null,
                        ),
                )
            }
        }

        if (state.loading == Done && state.error != null) {
            item(key = "error") {
                Text(
                    text = state.error.message ?: stringResource(R.string.page_title_unexpected_error),
                    color = TraktTheme.colors.textSecondary,
                    style = TraktTheme.typography.heading6,
                    modifier = Modifier.padding(horizontal = horizontalPadding),
                )
            }
        } else if (state.loading == Done && sections.isEmpty()) {
            item(key = "empty") {
                Text(
                    text = stringResource(R.string.button_text_no_services),
                    color = TraktTheme.colors.textSecondary,
                    style = TraktTheme.typography.heading6,
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

private fun ImmutableList<AllStreamingsSection>.filterByQuery(query: String): ImmutableList<AllStreamingsSection> {
    fun StreamingService.matchesCountry(query: String): Boolean {
        return country.contains(query, ignoreCase = true) ||
            countryName(country).contains(query, ignoreCase = true)
    }

    if (query.isBlank()) return this

    return mapNotNull { section ->
        val rows = section.rows.mapNotNull { row ->
            val serviceMatch = row.services.any { it.name.contains(query, ignoreCase = true) }
            if (serviceMatch) return@mapNotNull row

            val services = row.services.filter { it.matchesCountry(query) }
            when {
                services.isEmpty() -> null
                else -> row.copy(services = services.toImmutableList())
            }
        }
        when {
            rows.isEmpty() -> null
            else -> section.copy(rows = rows.toImmutableList())
        }
    }.toImmutableList()
}

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
                        type = Favorite,
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
                        type = Subscription,
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
