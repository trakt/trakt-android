package tv.trakt.trakt.core.shows

import HeaderBar
import ShowsTrendingView
import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.util.fastRoundToInt
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.core.shows.sections.anticipated.ShowsAnticipatedView
import tv.trakt.trakt.core.shows.sections.hot.ShowsHotView
import tv.trakt.trakt.core.shows.sections.popular.ShowsPopularView
import tv.trakt.trakt.ui.components.BackdropImage
import tv.trakt.trakt.ui.theme.TraktTheme

@Composable
internal fun ShowsScreen(onNavigateToShow: (TraktId) -> Unit) {
    ShowsScreenContent(
        onShowClick = onNavigateToShow,
    )
}

@Composable
private fun ShowsScreenContent(
    modifier: Modifier = Modifier,
    onShowClick: (TraktId) -> Unit,
) {
    val lazyListState = rememberLazyListState()

    val statusBarPaddings = WindowInsets.statusBars.asPaddingValues()
    val navigationBarPaddings = WindowInsets.navigationBars.asPaddingValues()

    val topPadding = statusBarPaddings
        .calculateTopPadding()
        .plus(TraktTheme.spacing.mainPageTopSpace)

    val bottomPadding = navigationBarPaddings
        .calculateBottomPadding()
        .plus(TraktTheme.size.navigationBarHeight)
        .plus(TraktTheme.spacing.mainPageBottomSpace)

    val sectionPadding = PaddingValues(
        start = TraktTheme.spacing.mainPageHorizontalSpace,
        end = TraktTheme.spacing.mainPageHorizontalSpace,
    )

    // Header

    val headerBarHeight = statusBarPaddings
        .calculateTopPadding()
        .plus(TraktTheme.size.navigationHeaderHeight)

    val headerBarMaxHeightPx = with(LocalDensity.current) {
        headerBarHeight.roundToPx()
    }

    val headerBarScrolled = remember { mutableStateOf(false) }
    val headerBarConnection = remember(headerBarMaxHeightPx) {
        CollapsingTopBarConnection(
            barMaxHeight = headerBarMaxHeightPx.toFloat(),
            onScrollUp = { headerBarScrolled.value = true },
        )
    }

    Box(
        contentAlignment = Alignment.TopStart,
        modifier = modifier
            .fillMaxSize()
            .nestedScroll(headerBarConnection),
    ) {
        BackdropImage(
            imageUrl = "",
            modifier = Modifier.graphicsLayer {
                if (lazyListState.firstVisibleItemIndex == 0) {
                    translationY = (-0.75F * lazyListState.firstVisibleItemScrollOffset)
                } else {
                    alpha = 0F
                }
            },
        )

        LazyColumn(
            state = lazyListState,
            verticalArrangement = spacedBy(TraktTheme.spacing.mainSectionVerticalSpace),
            contentPadding = PaddingValues(
                top = topPadding,
                bottom = bottomPadding,
            ),
        ) {
            item {
                ShowsTrendingView(
                    headerPadding = sectionPadding,
                    contentPadding = sectionPadding,
                )
            }

            item {
                ShowsHotView(
                    headerPadding = sectionPadding,
                    contentPadding = sectionPadding,
                )
            }

            item {
                ShowsAnticipatedView(
                    headerPadding = sectionPadding,
                    contentPadding = sectionPadding,
                )
            }

            item {
                ShowsPopularView(
                    headerPadding = sectionPadding,
                    contentPadding = sectionPadding,
                )
            }
        }

        HeaderBar(
            containerColor = if (!headerBarScrolled.value) {
                Color.Transparent
            } else {
                TraktTheme.colors.navigationHeaderContainer
            },
            modifier = Modifier
                .offset {
                    IntOffset(0, headerBarConnection.barOffset.fastRoundToInt())
                },
        )
    }
}

private class CollapsingTopBarConnection(
    val barMaxHeight: Float,
    val onScrollUp: () -> Unit = {},
) : NestedScrollConnection {
    var barOffset: Float by mutableFloatStateOf(0F)
        private set

    private var scrolledUp: Boolean = false

    override fun onPreScroll(
        available: Offset,
        source: NestedScrollSource,
    ): Offset {
        val delta = available.y
        val newOffset = barOffset + delta
        barOffset = newOffset.coerceIn(-barMaxHeight, 0F)

        if (!scrolledUp && newOffset <= -barMaxHeight) {
            onScrollUp()
            scrolledUp = true
        }

        return Offset.Zero
    }
}

@Preview(
    device = "id:pixel_6",
    showBackground = true,
    backgroundColor = 0xFF131517,
)
@Composable
private fun Preview() {
    TraktTheme {
        ShowsScreenContent(
            onShowClick = {},
        )
    }
}
