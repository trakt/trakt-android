@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)

package tv.trakt.trakt.core.summary.shows.features.trivia

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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.ColorImage
import coil3.annotation.ExperimentalCoilApi
import coil3.compose.AsyncImagePreviewHandler
import coil3.compose.LocalAsyncImagePreviewHandler
import kotlinx.collections.immutable.toImmutableList
import tv.trakt.trakt.common.helpers.LoadingState.Done
import tv.trakt.trakt.common.helpers.LoadingState.Idle
import tv.trakt.trakt.common.helpers.LoadingState.Loading
import tv.trakt.trakt.core.summary.ui.views.DetailsTrivia
import tv.trakt.trakt.core.summary.ui.views.DetailsTriviaSkeleton
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.components.TraktSectionHeader
import tv.trakt.trakt.ui.theme.TraktTheme

@Composable
internal fun ShowTriviaView(
    viewModel: ShowTriviaViewModel,
    headerPadding: PaddingValues,
    contentPadding: PaddingValues,
    onVipClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var visible by remember { mutableStateOf(true) }

    if (visible) {
        ShowTriviaContent(
            state = state,
            modifier = modifier,
            headerPadding = headerPadding,
            contentPadding = contentPadding,
            onCollapse = viewModel::setCollapsed,
            onNotAvailable = { visible = false },
            onVipClick = onVipClick,
        )
    }
}

@Composable
private fun ShowTriviaContent(
    state: ShowTriviaState,
    modifier: Modifier = Modifier,
    headerPadding: PaddingValues = PaddingValues(),
    contentPadding: PaddingValues = PaddingValues(),
    onCollapse: (collapsed: Boolean) -> Unit = {},
    onVipClick: () -> Unit = {},
    onNotAvailable: (() -> Unit)? = null,
) {
    var animateCollapse by rememberSaveable { mutableStateOf(false) }

    Column(
        verticalArrangement = spacedBy(TraktTheme.spacing.mainRowHeaderSpace),
        modifier = modifier
            .animateContentSize(
                animationSpec = if (animateCollapse) spring() else snap(),
            ),
    ) {
        TraktSectionHeader(
            title = stringResource(R.string.list_title_trivia),
            chevron = state.user?.isAnyVip == true,
            collapsed = state.collapsed ?: false,
            onCollapseClick = {
                animateCollapse = true
                val current = (state.collapsed ?: false)
                onCollapse(!current)
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(headerPadding),
        )

        if (state.collapsed != true) {
            Crossfade(
                targetState = state.loading,
                animationSpec = tween(200),
            ) { loading ->
                when (loading) {
                    Idle, Loading -> {
                        DetailsTriviaSkeleton(
                            modifier = Modifier.padding(contentPadding),
                        )
                    }
                    Done -> {
                        if (state.summary.isNullOrEmpty()) {
                            onNotAvailable?.invoke()
                        } else {
                            DetailsTrivia(
                                items = state.summary,
                                vip = state.user?.isAnyVip == true,
                                onVipClick = onVipClick,
                                modifier = Modifier.padding(contentPadding),
                            )
                        }
                    }
                }
            }
        }
    }
}

// -- Previews --

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
            ShowTriviaContent(
                state = ShowTriviaState(
                    summary = listOf(
                        "The famous chase scene was filmed in a single take over three days.",
                        "This scene reveals the true identity of the killer.",
                        "The lead actor improvised most of their dialogue in the final act.",
                    ).toImmutableList(),
                    loading = Done,
                ),
            )
        }
    }
}
