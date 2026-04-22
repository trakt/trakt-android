@file:OptIn(ExperimentalMaterial3Api::class)

package tv.trakt.trakt.core.home.sections.upnext.features.context

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow.Companion.Ellipsis
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import tv.trakt.trakt.common.helpers.LoadingState.Done
import tv.trakt.trakt.common.helpers.extensions.durationFormat
import tv.trakt.trakt.common.helpers.extensions.nowUtc
import tv.trakt.trakt.common.helpers.extensions.nowUtcInstant
import tv.trakt.trakt.common.helpers.preview.PreviewData
import tv.trakt.trakt.common.ui.theme.colors.Shade910
import tv.trakt.trakt.core.home.sections.upnext.model.Progress
import tv.trakt.trakt.core.home.sections.upnext.model.UpNextItem
import tv.trakt.trakt.core.home.sections.upnext.model.UpNextMovie
import tv.trakt.trakt.core.home.sections.upnext.model.UpNextShow
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.components.buttons.GhostButton
import tv.trakt.trakt.ui.components.confirmation.ConfirmationSheet
import tv.trakt.trakt.ui.theme.TraktTheme

@Composable
internal fun UpNextItemContextView(
    item: UpNextItem,
    viewModel: UpNextItemContextViewModel,
    modifier: Modifier = Modifier,
    onAddWatched: (UpNextItem) -> Unit,
    onDropped: (UpNextItem) -> Unit,
    onError: () -> Unit,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    var confirmDropSheet by remember { mutableStateOf(false) }

    LaunchedEffect(state.loadingWatched, state.loadingDrop) {
        when {
            state.loadingWatched == Done -> onAddWatched(item)
            state.loadingDrop == Done -> onDropped(item)
        }
    }

    LaunchedEffect(state.error) {
        if (state.error != null) {
            onError()
        }
    }

    UpNextItemContextViewContent(
        item = item,
        state = state,
        modifier = modifier,
        onAddWatched = {
            onAddWatched(item)
        },
        onRemoveWatchlist = {
            confirmDropSheet = true
        },
    )

    ConfirmationSheet(
        active = confirmDropSheet,
        onYes = {
            confirmDropSheet = false
            when (item) {
                is UpNextShow -> viewModel.dropShow(item.id)
                is UpNextMovie -> viewModel.dropMovie(item.id)
            }
        },
        onNo = { confirmDropSheet = false },
        title = stringResource(
            when (item) {
                is UpNextShow -> R.string.button_text_drop_show
                is UpNextMovie -> R.string.button_text_drop_movie
            },
        ),
        message = stringResource(
            when (item) {
                is UpNextShow -> R.string.warning_prompt_drop_show
                is UpNextMovie -> R.string.warning_prompt_drop_movie
            },
            when (item) {
                is UpNextShow -> item.show.title
                is UpNextMovie -> item.movie.title
            },
        ),
    )
}

@Composable
private fun UpNextItemContextViewContent(
    item: UpNextItem,
    state: UpNextItemContextState,
    modifier: Modifier = Modifier,
    onAddWatched: () -> Unit = {},
    onRemoveWatchlist: () -> Unit = {},
) {
    Column(
        verticalArrangement = spacedBy(0.dp),
        modifier = modifier,
    ) {
        Column(
            verticalArrangement = spacedBy(2.dp),
        ) {
            Text(
                text = when (item) {
                    is UpNextShow -> item.show.title
                    is UpNextMovie -> item.movie.title
                },
                color = TraktTheme.colors.textPrimary,
                style = TraktTheme.typography.heading3,
                maxLines = 1,
                overflow = Ellipsis,
                autoSize = TextAutoSize.StepBased(
                    maxFontSize = TraktTheme.typography.heading3.fontSize,
                    minFontSize = 20.sp,
                    stepSize = 2.sp,
                ),
            )

            Text(
                text = when (item) {
                    is UpNextShow -> item.progress.nextEpisode.seasonEpisodeString()
                    is UpNextMovie -> item.movie.runtime?.inWholeMinutes?.durationFormat() ?: ""
                },
                color = TraktTheme.colors.textSecondary,
                style = TraktTheme.typography.paragraphSmall,
            )
        }

        Spacer(
            modifier = Modifier
                .padding(top = 22.dp)
                .background(Shade910)
                .fillMaxWidth()
                .height(1.dp),
        )

        Column(
            verticalArrangement = spacedBy(TraktTheme.spacing.contextItemsSpace),
            modifier = Modifier.padding(top = 14.dp),
        ) {
            val isLoading =
                state.loadingWatched.isLoading ||
                    state.loadingDrop.isLoading

            if (item is UpNextShow) {
                GhostButton(
                    enabled = !isLoading,
                    loading = state.loadingWatched.isLoading,
                    text = stringResource(R.string.button_text_track),
                    iconSize = 20.dp,
                    iconSpace = 16.dp,
                    onClick = onAddWatched,
                    icon = painterResource(R.drawable.ic_check),
                    modifier = Modifier
                        .graphicsLayer {
                            translationX = -6.dp.toPx()
                        },
                )
            }

            GhostButton(
                enabled = !isLoading,
                loading = state.loadingDrop.isLoading,
                text = stringResource(
                    when (item) {
                        is UpNextShow -> R.string.button_text_drop_show
                        is UpNextMovie -> R.string.button_text_drop_movie
                    },
                ),
                onClick = onRemoveWatchlist,
                iconSize = 22.dp,
                iconSpace = 16.dp,
                icon = painterResource(R.drawable.ic_drop),
                modifier = Modifier
                    .graphicsLayer {
                        translationX = -8.dp.toPx()
                    },
            )
        }
    }
}

@Preview(
    device = "id:pixel_5",
    showBackground = true,
    backgroundColor = 0xFF131517,
)
@Composable
private fun Preview() {
    TraktTheme {
        UpNextItemContextViewContent(
            state = UpNextItemContextState(),
            item = UpNextShow(
                progress = Progress(
                    lastWatchedAt = nowUtc(),
                    aired = 12,
                    completed = 4,
                    stats = Progress.Stats(
                        playCount = 12,
                        minutesWatched = 120,
                        minutesLeft = 240,
                    ),
                    nextEpisode = PreviewData.episode1,
                    lastEpisode = null,
                ),
                show = PreviewData.show1,
            ),
        )
    }
}

@Preview(
    device = "id:pixel_5",
    showBackground = true,
    backgroundColor = 0xFF131517,
)
@Composable
private fun Preview2() {
    TraktTheme {
        UpNextItemContextViewContent(
            state = UpNextItemContextState(),
            item = UpNextMovie(
                movie = PreviewData.movie1,
                progress = UpNextMovie.Progress(
                    id = 1,
                    progress = 33F,
                    pausedAt = nowUtcInstant(),
                ),
            ),
        )
    }
}
