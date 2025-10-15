package tv.trakt.trakt.core.home.sections.activity.views.context

import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.ColorImage
import coil3.annotation.ExperimentalCoilApi
import coil3.compose.AsyncImagePreviewHandler
import coil3.compose.LocalAsyncImagePreviewHandler
import tv.trakt.trakt.common.helpers.LoadingState.DONE
import tv.trakt.trakt.common.helpers.extensions.relativePastDateString
import tv.trakt.trakt.common.helpers.extensions.toLocal
import tv.trakt.trakt.common.helpers.preview.PreviewData
import tv.trakt.trakt.common.model.Images.Size.THUMB
import tv.trakt.trakt.common.ui.theme.colors.Shade910
import tv.trakt.trakt.core.home.sections.activity.model.HomeActivityItem
import tv.trakt.trakt.core.home.sections.activity.model.HomeActivityItem.EpisodeItem
import tv.trakt.trakt.core.home.sections.activity.model.HomeActivityItem.MovieItem
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.components.buttons.GhostButton
import tv.trakt.trakt.ui.components.confirmation.ConfirmationSheet
import tv.trakt.trakt.ui.components.mediacards.PanelMediaCard
import tv.trakt.trakt.ui.theme.TraktTheme
import java.time.Instant

@Composable
internal fun ActivityItemContextView(
    item: HomeActivityItem,
    viewModel: ActivityItemContextViewModel,
    modifier: Modifier = Modifier,
    onPlayRemove: () -> Unit,
    onAddWatchlist: () -> Unit,
    onError: () -> Unit,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    var confirmRemoveSheet by remember { mutableStateOf(false) }

    LaunchedEffect(state.loadingRemove, state.loadingWatchlist) {
        when {
            state.loadingRemove == DONE -> onPlayRemove()
            state.loadingWatchlist == DONE -> onAddWatchlist()
        }
    }

    LaunchedEffect(state.error) {
        if (state.error != null) {
            onError()
        }
    }

    ActivityItemContextViewContent(
        item = item,
        state = state,
        modifier = modifier,
        onRemoveWatchedClick = {
            confirmRemoveSheet = true
        },
    )

    @OptIn(ExperimentalMaterial3Api::class)
    ConfirmationSheet(
        active = confirmRemoveSheet,
        onYes = {
            confirmRemoveSheet = false
            viewModel.removePlayFromHistory(item)
        },
        onNo = { confirmRemoveSheet = false },
        title = stringResource(R.string.button_text_remove_from_history),
        message = stringResource(
            R.string.warning_prompt_remove_single_watched,
            when (item) {
                is EpisodeItem -> item.episode.title
                is MovieItem -> item.movie.title
            },
        ),
    )
}

@Composable
private fun ActivityItemContextViewContent(
    item: HomeActivityItem,
    state: ActivityItemContextState,
    modifier: Modifier = Modifier,
    onRemoveWatchedClick: () -> Unit = {},
) {
    Column(
        verticalArrangement = spacedBy(0.dp),
        modifier = modifier,
    ) {
        PanelMediaCard(
            title = item.title,
            titleOriginal = item.titleOriginal,
            subtitle = when (item) {
                is EpisodeItem -> item.episode.seasonEpisodeString()
                is MovieItem -> stringResource(R.string.translated_value_type_movie)
            },
            shadow = 4.dp,
            containerColor = Shade910,
            contentImageUrl = item.images?.getPosterUrl(),
            containerImageUrl = when (item) {
                is EpisodeItem -> item.episode.images?.getScreenshotUrl(THUMB)
                    ?: item.episode.images?.getFanartUrl(THUMB)
                is MovieItem -> item.movie.images?.getFanartUrl(THUMB)
            },
            footerContent = {
                Row(
                    horizontalArrangement = spacedBy(3.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_calendar_check),
                        contentDescription = null,
                        tint = TraktTheme.colors.textSecondary,
                        modifier = Modifier.size(14.dp),
                    )
                    Text(
                        text = item.activityAt.toLocal().relativePastDateString(),
                        color = TraktTheme.colors.textSecondary,
                        style = TraktTheme.typography.meta.copy(fontSize = 12.sp),
                    )
                }
            },
        )

        Column(
            verticalArrangement = spacedBy(TraktTheme.spacing.contextItemsSpace),
            modifier = Modifier
                .padding(top = 24.dp),
        ) {
            GhostButton(
                enabled = !state.loadingRemove.isLoading,
                loading = state.loadingRemove.isLoading,
                text = stringResource(R.string.button_text_remove_from_history),
                onClick = onRemoveWatchedClick,
                icon = painterResource(R.drawable.ic_close),
                iconSize = 24.dp,
                modifier = Modifier
                    .graphicsLayer {
                        translationX = -12.dp.toPx()
                    },
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
private fun Preview() {
    TraktTheme {
        val previewHandler = AsyncImagePreviewHandler {
            ColorImage(Color.Blue.toArgb())
        }
        CompositionLocalProvider(LocalAsyncImagePreviewHandler provides previewHandler) {
            ActivityItemContextViewContent(
                state = ActivityItemContextState(),
                item = EpisodeItem(
                    id = 1L,
                    user = PreviewData.user1,
                    activity = "watched",
                    activityAt = Instant.now(),
                    episode = PreviewData.episode1,
                    show = PreviewData.show1,
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
private fun Preview2() {
    TraktTheme {
        val previewHandler = AsyncImagePreviewHandler {
            ColorImage(Color.Blue.toArgb())
        }
        CompositionLocalProvider(LocalAsyncImagePreviewHandler provides previewHandler) {
            ActivityItemContextViewContent(
                state = ActivityItemContextState(),
                item = MovieItem(
                    id = 1L,
                    user = PreviewData.user1,
                    activity = "watched",
                    activityAt = Instant.now(),
                    movie = PreviewData.movie1,
                ),
            )
        }
    }
}
