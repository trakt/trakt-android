package tv.trakt.trakt.core.summary.sentiment

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Arrangement.Absolute.spacedBy
import androidx.compose.foundation.layout.Arrangement.SpaceBetween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
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
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RadialGradientShader
import androidx.compose.ui.graphics.Shader
import androidx.compose.ui.graphics.ShaderBrush
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight.Companion.W600
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.ColorImage
import coil3.annotation.ExperimentalCoilApi
import coil3.compose.AsyncImagePreviewHandler
import coil3.compose.LocalAsyncImagePreviewHandler
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import tv.trakt.trakt.common.helpers.extensions.onClick
import tv.trakt.trakt.common.helpers.extensions.toMarkdownText
import tv.trakt.trakt.common.helpers.preview.PreviewData
import tv.trakt.trakt.common.model.Sentiments
import tv.trakt.trakt.common.model.Sentiments.Overall.Positive
import tv.trakt.trakt.common.model.SlugId
import tv.trakt.trakt.common.ui.theme.colors.Shade920
import tv.trakt.trakt.core.applinks.rememberAppLinkUriHandler
import tv.trakt.trakt.core.billing.model.VipUpsellSource
import tv.trakt.trakt.core.summary.sentiment.model.InsightsTab
import tv.trakt.trakt.core.summary.sentiment.model.RealityCheckSection
import tv.trakt.trakt.core.summary.sentiment.views.InsightsSegments
import tv.trakt.trakt.core.summary.sentiment.views.RealityCheckContent
import tv.trakt.trakt.core.summary.sentiment.views.markdownSections
import tv.trakt.trakt.helpers.SimpleScrollConnection
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.components.ScrollableBackdropImage
import tv.trakt.trakt.ui.components.TraktHeader
import tv.trakt.trakt.ui.components.chips.FilterChip
import tv.trakt.trakt.ui.components.chips.FilterChipGroup
import tv.trakt.trakt.ui.components.vip.VipChip
import tv.trakt.trakt.ui.components.vip.onUpsellSeen
import tv.trakt.trakt.ui.theme.DefaultCardShape
import tv.trakt.trakt.ui.theme.TraktTheme

@Composable
internal fun SentimentScreen(
    viewModel: SentimentViewModel,
    onNavigateVip: (source: VipUpsellSource) -> Unit,
    onNavigateToPerson: (SlugId) -> Unit,
    onNavigateBack: () -> Unit,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    SentimentContent(
        state = state,
        onTabClick = viewModel::selectTab,
        onVipClick = { onNavigateVip(state.tab.upsellSource) },
        onVipSeen = viewModel::onVipUpsellSeen,
        onSectionCollapse = viewModel::setSectionCollapsed,
        onPersonLinkClick = onNavigateToPerson,
        onBackClick = onNavigateBack,
    )
}

@Composable
private fun SentimentContent(
    state: SentimentState,
    onTabClick: (InsightsTab) -> Unit = {},
    onVipClick: () -> Unit = {},
    onVipSeen: () -> Unit = {},
    onSectionCollapse: (RealityCheckSection, Boolean) -> Unit = { _, _ -> },
    onPersonLinkClick: (SlugId) -> Unit = {},
    onBackClick: () -> Unit = {},
) {
    val listState = rememberLazyListState()
    val linkUriHandler = rememberAppLinkUriHandler(onPersonClick = onPersonLinkClick)

    val listScrollConnection = rememberSaveable(saver = SimpleScrollConnection.Saver) {
        SimpleScrollConnection()
    }

    val contentPadding = PaddingValues(
        start = TraktTheme.spacing.mainPageHorizontalSpace,
        end = TraktTheme.spacing.mainPageHorizontalSpace,
        top = WindowInsets.statusBars.asPaddingValues().calculateTopPadding(),
        bottom = WindowInsets.navigationBars.asPaddingValues()
            .calculateBottomPadding()
            .plus(TraktTheme.size.navigationBarHeight)
            .plus(TraktTheme.spacing.mainPageBottomSpace),
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(TraktTheme.colors.backgroundPrimary)
            .nestedScroll(listScrollConnection),
    ) {
        ScrollableBackdropImage(
            translation = listScrollConnection.resultOffset,
            imageUrl = state.backgroundUrl,
        )

        CompositionLocalProvider(LocalUriHandler provides linkUriHandler) {
            LazyColumn(
                state = listState,
                verticalArrangement = spacedBy(0.dp),
                contentPadding = contentPadding,
                overscrollEffect = null,
            ) {
                item {
                    TitleBar(
                        mediaTitle = state.mediaTitle,
                        modifier = Modifier
                            .onClick { onBackClick() },
                    )
                }

                if (state.tabs.size > 1) {
                    item {
                        InsightsTabs(
                            tabs = state.tabs,
                            selected = state.tab,
                            onTabClick = onTabClick,
                        )
                    }
                }

                when (state.tab) {
                    InsightsTab.Sentiment -> {
                        state.sentiments?.let { sentiments ->
                            sentimentItems(
                                sentiments = sentiments,
                                vip = state.user?.isAnyVip == true,
                                onVipClick = onVipClick,
                                onVipSeen = onVipSeen,
                            )
                        }
                    }

                    InsightsTab.RealityCheck -> {
                        state.realityCheck?.let { realityCheck ->
                            item {
                                RealityCheckContent(
                                    realityCheck = realityCheck,
                                    vip = state.user?.isAnyVip == true,
                                    collapsedSections = state.collapsedSections,
                                    onSectionCollapse = onSectionCollapse,
                                    upsell = {
                                        GetVipView(
                                            modifier = Modifier
                                                .onUpsellSeen(onVipSeen)
                                                .onClick(onClick = onVipClick),
                                        )
                                    },
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun LazyListScope.sentimentItems(
    sentiments: Sentiments,
    vip: Boolean,
    onVipClick: () -> Unit,
    onVipSeen: () -> Unit,
) {
    if (!vip) {
        item {
            Column {
                GetVipView(
                    modifier = Modifier
                        .onUpsellSeen(onVipSeen)
                        .padding(top = 12.dp)
                        .onClick(onClick = onVipClick),
                )

                Row(
                    horizontalArrangement = spacedBy(4.dp, CenterHorizontally),
                    verticalAlignment = CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = TraktTheme.spacing.mainPageHorizontalSpace)
                        .padding(top = 24.dp, bottom = 8.dp),
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_info),
                        tint = TraktTheme.colors.textSecondary,
                        contentDescription = null,
                        modifier = Modifier
                            .size(16.dp),
                    )
                    Text(
                        text = stringResource(R.string.text_sentiment_example, "Star Wars: The Last Jedi"),
                        color = TraktTheme.colors.textSecondary,
                        style = TraktTheme.typography.heading6,
                        textAlign = TextAlign.Center,
                        modifier = Modifier,
                    )
                }
            }
        }
    }

    if (sentiments.analysis.isNotBlank()) {
        item {
            val linkColor = TraktTheme.colors.textPrimary
            Text(
                text = remember(sentiments.analysis, linkColor) {
                    sentiments.analysis.toMarkdownText(linkColor)
                },
                color = TraktTheme.colors.textPrimary,
                style = TraktTheme.typography.paragraphSmall.copy(lineHeight = 1.4.em),
                modifier = Modifier.padding(top = 16.dp),
            )
        }
    }

    if (sentiments.highlight.isNotBlank()) {
        item {
            HighlightsCard(
                header = stringResource(R.string.header_sentiment_highlight),
                text = sentiments.highlight,
                modifier = Modifier.padding(top = 20.dp),
            )
        }
    }

    if (sentiments.pros.isNotEmpty() || sentiments.cons.isNotEmpty()) {
        item {
            ProsConsCard(
                header = stringResource(R.string.header_sentiment_aspects),
                pros = sentiments.pros,
                cons = sentiments.cons,
                modifier = Modifier.padding(top = 20.dp),
            )
        }
    }
}

@Composable
private fun InsightsTabs(
    tabs: ImmutableList<InsightsTab>,
    selected: InsightsTab,
    onTabClick: (InsightsTab) -> Unit,
) {
    FilterChipGroup(
        paddingVertical = PaddingValues(top = 6.dp),
    ) {
        tabs.forEach { tab ->
            FilterChip(
                selected = tab == selected,
                text = stringResource(tab.displayTextRes),
                height = 32.dp,
                onClick = { onTabClick(tab) },
            )
        }
    }
}

@Composable
private fun TitleBar(
    mediaTitle: String?,
    modifier: Modifier = Modifier,
) {
    Row(
        verticalAlignment = CenterVertically,
        horizontalArrangement = SpaceBetween,
        modifier = modifier.fillMaxWidth(),
    ) {
        Row(
            verticalAlignment = CenterVertically,
            horizontalArrangement = spacedBy(12.dp),
            modifier = Modifier
                .height(TraktTheme.size.titleBarHeight)
                .graphicsLayer { translationX = -2.dp.toPx() },
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_back_arrow),
                tint = TraktTheme.colors.textPrimary,
                contentDescription = null,
            )
            TraktHeader(
                title = stringResource(R.string.header_insights),
                subtitle = mediaTitle,
            )
        }
    }
}

@Composable
internal fun HighlightsCard(
    header: String,
    text: String,
    modifier: Modifier = Modifier,
    collapsed: Boolean? = null,
    onCollapseClick: () -> Unit = {},
) {
    val sections = remember(text) { text.markdownSections() }
    val linkColor = TraktTheme.colors.textPrimary

    InsightsSegments(
        header = header,
        count = sections.size,
        modifier = modifier,
        collapsed = collapsed,
        onCollapseClick = onCollapseClick,
    ) { index ->
        val section = sections[index]
        val style = TraktTheme.typography.paragraphSmall.copy(lineHeight = 1.35.em)
        Column(
            verticalArrangement = spacedBy(6.dp),
        ) {
            section.title?.let { title ->
                Text(
                    text = remember(title, linkColor) {
                        title.toMarkdownText(
                            linkColor = linkColor,
                            headingColor = linkColor,
                        )
                    },
                    color = TraktTheme.colors.textPrimary,
                    style = style,
                )
            }
            if (section.body.isNotEmpty()) {
                Text(
                    text = remember(section.body, linkColor) {
                        section.body.toMarkdownText(linkColor = linkColor)
                    },
                    color = TraktTheme.colors.textSecondary,
                    style = style,
                )
            }
        }
    }
}

@Composable
internal fun ProsConsCard(
    header: String,
    pros: ImmutableList<Sentiments.Theme>,
    cons: ImmutableList<Sentiments.Theme>,
    modifier: Modifier = Modifier,
) {
    val goodColor = TraktTheme.colors.sentimentsGoodAccent
    val badColor = TraktTheme.colors.sentimentsBadAccent

    val groups = listOfNotNull(
        Triple(R.drawable.ic_thumb_up_fill, goodColor, pros).takeIf { pros.isNotEmpty() },
        Triple(R.drawable.ic_thumb_down_fill, badColor, cons).takeIf { cons.isNotEmpty() },
    )

    InsightsSegments(
        header = header,
        count = groups.size,
        modifier = modifier,
    ) { index ->
        val (icon, color, themes) = groups[index]
        Row(
            horizontalArrangement = spacedBy(12.dp),
            verticalAlignment = Alignment.Top,
        ) {
            Icon(
                painter = painterResource(icon),
                tint = color,
                contentDescription = null,
                modifier = Modifier.size(22.dp),
            )
            Column(
                verticalArrangement = spacedBy(12.dp),
            ) {
                themes.forEach { theme ->
                    SentimentThemeRow(
                        text = theme.value,
                        color = color,
                    )
                }
            }
        }
    }
}

@Composable
private fun SentimentThemeRow(
    text: String,
    color: Color,
    modifier: Modifier = Modifier,
) {
    Row(
        horizontalArrangement = spacedBy(6.dp),
        verticalAlignment = Alignment.Top,
        modifier = modifier,
    ) {
        Text(
            text = "•",
            color = color,
            style = TraktTheme.typography.paragraphSmall,
        )
        Text(
            text = text,
            color = color,
            style = TraktTheme.typography.paragraphSmall,
        )
    }
}

@Composable
internal fun GetVipView(modifier: Modifier = Modifier) {
    val gradientAccent = TraktTheme.colors.accent
    val radialGradient = remember(gradientAccent) {
        object : ShaderBrush() {
            override fun createShader(size: Size): Shader {
                return RadialGradientShader(
                    colors = listOf(
                        gradientAccent,
                        Shade920,
                    ),
                    center = Offset(size.width * 1.5F, -size.height / 1.5F),
                    radius = size.width * 1.5F,
                )
            }
        }
    }

    Row(
        verticalAlignment = CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 8.dp, bottom = 4.dp)
            .clip(DefaultCardShape)
            .background(radialGradient)
            .padding(start = 16.dp, end = 14.dp)
            .padding(vertical = 12.dp),
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(3.dp),
            modifier = Modifier.weight(1f),
        ) {
            Text(
                text = stringResource(R.string.text_vip_upsell_dive_deeper),
                color = TraktTheme.colors.textPrimaryOnAccent,
                style = TraktTheme.typography.paragraphSmall.copy(
                    fontWeight = W600,
                ),
            )
            Text(
                text = stringResource(R.string.text_vip_upsell_sentiment),
                color = TraktTheme.colors.textPrimaryOnAccent,
                style = TraktTheme.typography.paragraphSmaller.copy(
                    fontSize = 12.sp,
                ),
            )
        }

        VipChip(
            text = stringResource(R.string.badge_text_get_vip),
        )
    }
}

@OptIn(ExperimentalCoilApi::class)
@Preview(
    device = "id:pixel_5",
    showBackground = true,
    backgroundColor = 0xFF131517,
)
@Composable
private fun PreviewNonVip() {
    TraktTheme {
        val previewHandler = AsyncImagePreviewHandler {
            ColorImage(Color.Blue.toArgb())
        }
        CompositionLocalProvider(LocalAsyncImagePreviewHandler provides previewHandler) {
            SentimentContent(
                state = SentimentState(
                    sentiments = Sentiments(
                        overall = Positive,
                        analysis = "Audiences praise the compelling characters and tight pacing. " +
                            "The show holds up well across multiple seasons with few dull moments.",
                        highlight = "One of the most rewatchable series of the decade.",
                        pros = listOf(
                            Sentiments.Theme(value = "Compelling characters", confidence = 0.95f),
                            Sentiments.Theme(value = "Tight pacing", confidence = 0.88f),
                            Sentiments.Theme(value = "Strong writing", confidence = 0.82f),
                        ).toImmutableList(),
                        cons = listOf(
                            Sentiments.Theme(value = "Slow start in season 2", confidence = 0.61f),
                        ).toImmutableList(),
                    ),
                    realityCheck = PreviewData.realityCheck.copy(detail = null),
                    tabs = persistentListOf(InsightsTab.Sentiment, InsightsTab.RealityCheck),
                    tab = InsightsTab.Sentiment,
                    mediaTitle = "Breaking Bad",
                    backgroundUrl = null,
                ),
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
private fun PreviewVip() {
    TraktTheme {
        val previewHandler = AsyncImagePreviewHandler {
            ColorImage(Color.Blue.toArgb())
        }
        CompositionLocalProvider(LocalAsyncImagePreviewHandler provides previewHandler) {
            SentimentContent(
                state = SentimentState(
                    user = PreviewData.user1.copy(
                        isVip = true,
                    ),
                    sentiments = Sentiments(
                        overall = Positive,
                        analysis = "Audiences praise the compelling characters and tight pacing. " +
                            "The show holds up well across multiple seasons with few dull moments.",
                        highlight = "One of the most rewatchable series of the decade.",
                        pros = listOf(
                            Sentiments.Theme(value = "Compelling characters", confidence = 0.95f),
                            Sentiments.Theme(value = "Tight pacing", confidence = 0.88f),
                            Sentiments.Theme(value = "Strong writing", confidence = 0.82f),
                        ).toImmutableList(),
                        cons = listOf(
                            Sentiments.Theme(value = "Slow start in season 2", confidence = 0.61f),
                        ).toImmutableList(),
                    ),
                    realityCheck = PreviewData.realityCheck,
                    tabs = persistentListOf(InsightsTab.Sentiment, InsightsTab.RealityCheck),
                    tab = InsightsTab.RealityCheck,
                    mediaTitle = "Breaking Bad",
                    backgroundUrl = null,
                ),
            )
        }
    }
}
