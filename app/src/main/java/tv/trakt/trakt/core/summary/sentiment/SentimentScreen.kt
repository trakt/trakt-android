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
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults.cardColors
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
import kotlinx.collections.immutable.toImmutableList
import tv.trakt.trakt.common.helpers.extensions.onClick
import tv.trakt.trakt.common.helpers.preview.PreviewData
import tv.trakt.trakt.common.model.Sentiments
import tv.trakt.trakt.common.model.Sentiments.Overall.Positive
import tv.trakt.trakt.common.ui.theme.colors.Red500
import tv.trakt.trakt.common.ui.theme.colors.Shade920
import tv.trakt.trakt.helpers.SimpleScrollConnection
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.components.ScrollableBackdropImage
import tv.trakt.trakt.ui.components.TraktHeader
import tv.trakt.trakt.ui.components.vip.VipChip
import tv.trakt.trakt.ui.theme.DefaultCardShape
import tv.trakt.trakt.ui.theme.TraktTheme

@Composable
internal fun SentimentScreen(
    viewModel: SentimentViewModel,
    onNavigateVip: () -> Unit,
    onNavigateBack: () -> Unit,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    SentimentContent(
        state = state,
        onVipClick = onNavigateVip,
        onBackClick = onNavigateBack,
    )
}

@Composable
private fun SentimentContent(
    state: SentimentState,
    onVipClick: () -> Unit = {},
    onBackClick: () -> Unit = {},
) {
    val listState = rememberLazyListState()

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

        LazyColumn(
            state = listState,
            verticalArrangement = spacedBy(0.dp),
            contentPadding = contentPadding,
            overscrollEffect = null,
        ) {
            item {
                TitleBar(
                    mediaTitle = state.mediaTitle,
                    modifier = Modifier.onClick { onBackClick() },
                )
            }

            if (state.user?.isAnyVip != true) {
                item {
                    Column {
                        GetVipView(
                            modifier = Modifier
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

            val sentiments = state.sentiments
            if (sentiments.analysis.isNotBlank()) {
                item {
                    Text(
                        text = sentiments.analysis,
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
    }
}

@Composable
private fun TitleBar(
    mediaTitle: String?,
    modifier: Modifier = Modifier,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = SpaceBetween,
        modifier = modifier.fillMaxWidth(),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
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
                title = stringResource(R.string.header_community_sentiment),
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
) {
    Card(
        modifier = modifier,
        shape = DefaultCardShape,
        colors = cardColors(
            containerColor = TraktTheme.colors.commentContainer,
        ),
    ) {
        Column(
            verticalArrangement = spacedBy(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
        ) {
            Text(
                text = header,
                color = TraktTheme.colors.textPrimary,
                style = TraktTheme.typography.heading5,
            )

            Text(
                text = text,
                color = TraktTheme.colors.textPrimary,
                style = TraktTheme.typography.paragraphSmall.copy(lineHeight = 1.4.em),
            )
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

    Card(
        modifier = modifier,
        shape = DefaultCardShape,
        colors = cardColors(
            containerColor = TraktTheme.colors.commentContainer,
        ),
    ) {
        Column(
            verticalArrangement = spacedBy(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
        ) {
            Text(
                text = header,
                color = TraktTheme.colors.textPrimary,
                style = TraktTheme.typography.heading5,
            )

            if (pros.isNotEmpty()) {
                Row(
                    horizontalArrangement = spacedBy(12.dp),
                    verticalAlignment = Alignment.Top,
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_thumb_up2_fill),
                        tint = goodColor,
                        contentDescription = null,
                        modifier = Modifier.size(22.dp),
                    )
                    Column(
                        verticalArrangement = spacedBy(12.dp),
                    ) {
                        pros.forEach { theme ->
                            SentimentThemeRow(
                                text = theme.value,
                                color = goodColor,
                            )
                        }
                    }
                }
            }

            if (cons.isNotEmpty()) {
                Row(
                    horizontalArrangement = spacedBy(12.dp),
                    verticalAlignment = Alignment.Top,
                    modifier = Modifier.padding(top = 8.dp),
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_thumb_down2_fill),
                        tint = badColor,
                        contentDescription = null,
                        modifier = Modifier.size(22.dp),
                    )
                    Column(
                        verticalArrangement = spacedBy(12.dp),
                    ) {
                        cons.forEach { theme ->
                            SentimentThemeRow(
                                text = theme.value,
                                color = badColor,
                            )
                        }
                    }
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
private fun GetVipView(modifier: Modifier = Modifier) {
    val radialGradient = remember {
        object : ShaderBrush() {
            override fun createShader(size: Size): Shader {
                return RadialGradientShader(
                    colors = listOf(
                        Red500,
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
                color = TraktTheme.colors.textPrimary,
                style = TraktTheme.typography.paragraphSmall.copy(
                    fontWeight = W600,
                ),
            )
            Text(
                text = stringResource(R.string.text_vip_upsell_sentiment),
                color = TraktTheme.colors.textPrimary,
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
                    mediaTitle = "Breaking Bad",
                    backgroundUrl = null,
                ),
            )
        }
    }
}
