@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)

package tv.trakt.trakt.core.summary.shows.features.sentiment

import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.collections.immutable.toImmutableList
import tv.trakt.trakt.common.helpers.LoadingState.Done
import tv.trakt.trakt.common.helpers.LoadingState.Idle
import tv.trakt.trakt.common.helpers.LoadingState.Loading
import tv.trakt.trakt.common.helpers.extensions.PreviewTraktLightDark
import tv.trakt.trakt.common.helpers.extensions.onClick
import tv.trakt.trakt.common.helpers.preview.PreviewData
import tv.trakt.trakt.common.model.Sentiments
import tv.trakt.trakt.common.model.Sentiments.Overall.Positive
import tv.trakt.trakt.core.summary.sentiment.MediaSentimentState
import tv.trakt.trakt.core.summary.sentiment.MediaSentimentViewModel
import tv.trakt.trakt.core.summary.sentiment.model.InsightsTab
import tv.trakt.trakt.core.summary.sentiment.model.MediaInsights
import tv.trakt.trakt.core.summary.ui.views.insights.DetailsSentiment
import tv.trakt.trakt.core.summary.ui.views.insights.DetailsSentimentSkeleton
import tv.trakt.trakt.helpers.extensions.TraktThemePreview
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.components.TraktSectionHeader
import tv.trakt.trakt.ui.extensions.isAtLeastLarge
import tv.trakt.trakt.ui.theme.TraktTheme

@Composable
internal fun ShowSentimentView(
    viewModel: MediaSentimentViewModel,
    headerPadding: PaddingValues,
    contentPadding: PaddingValues,
    modifier: Modifier = Modifier,
    onSentimentClick: (MediaInsights) -> Unit = {},
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var visible by remember { mutableStateOf(true) }

    if (visible) {
        ShowSentimentContent(
            state = state,
            modifier = modifier,
            headerPadding = headerPadding,
            contentPadding = contentPadding,
            onCollapse = viewModel::setCollapsed,
            onNotAvailable = { visible = false },
            onSentimentClick = onSentimentClick,
        )
    }
}

@Composable
private fun ShowSentimentContent(
    state: MediaSentimentState,
    modifier: Modifier = Modifier,
    headerPadding: PaddingValues = PaddingValues(),
    contentPadding: PaddingValues = PaddingValues(),
    onSentimentClick: (MediaInsights) -> Unit = {},
    onCollapse: (collapsed: Boolean) -> Unit = {},
    onNotAvailable: (() -> Unit)? = null,
) {
    val windowClass = currentWindowAdaptiveInfo().windowSizeClass
    var animateCollapse by rememberSaveable { mutableStateOf(false) }

    Column(
        verticalArrangement = spacedBy(TraktTheme.spacing.mainRowHeaderSpace),
        modifier = modifier
            .animateContentSize(
                animationSpec = if (animateCollapse) spring() else snap(),
            ),
    ) {
        TraktSectionHeader(
            title = stringResource(R.string.header_insights),
            chevron = false,
            collapsed = state.collapsed ?: false,
            onCollapseClick = {
                animateCollapse = true
                val current = (state.collapsed ?: false)
                onCollapse(!current)
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(headerPadding)
                .onClick {
                    if (state.hasInsights) {
                        onSentimentClick(state.insights())
                    }
                },
        )

        if (state.collapsed != true) {
            Crossfade(
                targetState = state.loading,
                animationSpec = tween(200),
                modifier = Modifier.padding(bottom = TraktTheme.spacing.shadowClipSpace),
            ) { loading ->
                val maxWidth = if (windowClass.isAtLeastLarge()) 0.45f else 1f
                when (loading) {
                    Idle, Loading -> {
                        DetailsSentimentSkeleton(
                            modifier = Modifier
                                .fillMaxWidth(maxWidth)
                                .padding(contentPadding),
                        )
                    }
                    Done -> {
                        if (state.hasInsights) {
                            DetailsSentiment(
                                vip = state.user?.isAnyVip == true,
                                sentiments = state.sentiment,
                                realityCheck = state.realityCheck,
                                modifier = Modifier
                                    .fillMaxWidth(maxWidth)
                                    .padding(contentPadding)
                                    .onClick {
                                        onSentimentClick(state.insights())
                                    },
                                onRealityCheckClick = {
                                    onSentimentClick(state.insights(InsightsTab.RealityCheck))
                                },
                            )
                        } else {
                            onNotAvailable?.invoke()
                        }
                    }
                }
            }
        }
    }
}

// -- Previews --

private val previewSentiments = Sentiments(
    overall = Positive,
    pros = listOf(
        Sentiments.Theme(value = "Lorem ipsum dolor sit amet consectetur", confidence = 0.9F),
        Sentiments.Theme(value = "Stylish", confidence = 0.8F),
    ).toImmutableList(),
    cons = listOf(
        Sentiments.Theme(value = "Lorem ipsum dolor sit amet consectetur", confidence = 0.9F),
        Sentiments.Theme(value = "Slow pacing", confidence = 0.7F),
    ).toImmutableList(),
)

private val previewVipUser = PreviewData.user1.copy(isVip = true)

@PreviewTraktLightDark
@Composable
private fun PreviewLoading() {
    TraktThemePreview {
        ShowSentimentContent(
            state = MediaSentimentState(loading = Loading),
        )
    }
}

@PreviewTraktLightDark
@Composable
private fun PreviewVip() {
    TraktThemePreview {
        ShowSentimentContent(
            state = MediaSentimentState(
                user = previewVipUser,
                sentiment = previewSentiments,
                realityCheck = PreviewData.realityCheck,
                loading = Done,
            ),
        )
    }
}

@PreviewTraktLightDark
@Composable
private fun PreviewNonVip() {
    TraktThemePreview {
        ShowSentimentContent(
            state = MediaSentimentState(
                user = PreviewData.user1,
                sentiment = previewSentiments,
                realityCheck = PreviewData.realityCheck,
                loading = Done,
            ),
        )
    }
}

@PreviewTraktLightDark
@Composable
private fun PreviewRealityCheckOnly() {
    TraktThemePreview {
        ShowSentimentContent(
            state = MediaSentimentState(
                user = previewVipUser,
                realityCheck = PreviewData.realityCheck,
                loading = Done,
            ),
        )
    }
}

@PreviewTraktLightDark
@Composable
private fun PreviewCollapsed() {
    TraktThemePreview {
        ShowSentimentContent(
            state = MediaSentimentState(
                sentiment = previewSentiments,
                loading = Done,
                collapsed = true,
            ),
        )
    }
}

@Preview(
    device = "id:pixel_tablet",
    showBackground = true,
    backgroundColor = 0xFF131517,
)
@Composable
private fun PreviewTablet() {
    TraktTheme {
        ShowSentimentContent(
            state = MediaSentimentState(
                user = previewVipUser,
                sentiment = previewSentiments,
                realityCheck = PreviewData.realityCheck,
                loading = Done,
            ),
        )
    }
}
