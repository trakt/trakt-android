package tv.trakt.trakt.core.home.sections.upnext.views

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.ColorImage
import coil3.annotation.ExperimentalCoilApi
import coil3.compose.AsyncImagePreviewHandler
import coil3.compose.LocalAsyncImagePreviewHandler
import tv.trakt.trakt.common.helpers.LoadingState.DONE
import tv.trakt.trakt.common.helpers.extensions.nowUtc
import tv.trakt.trakt.common.ui.theme.colors.Shade910
import tv.trakt.trakt.core.home.sections.upnext.model.Progress
import tv.trakt.trakt.core.home.sections.upnext.model.ProgressShow
import tv.trakt.trakt.helpers.preview.PreviewData
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.components.buttons.GhostButton
import tv.trakt.trakt.ui.components.confirmation.ConfirmationSheet
import tv.trakt.trakt.ui.components.mediacards.HorizontalMediaCard
import tv.trakt.trakt.ui.theme.TraktTheme

@Composable
internal fun UpNextItemContextView(
    item: ProgressShow,
    viewModel: UpNextItemContextViewModel,
    modifier: Modifier = Modifier,
    onAddWatched: (ProgressShow) -> Unit,
    onDropShow: (ProgressShow) -> Unit,
    onError: () -> Unit,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    var confirmDropSheet by remember { mutableStateOf(false) }

    LaunchedEffect(state.loadingWatched, state.loadingDrop) {
        when {
            state.loadingWatched == DONE -> onAddWatched(item)
            state.loadingDrop == DONE -> onDropShow(item)
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

    @OptIn(ExperimentalMaterial3Api::class)
    ConfirmationSheet(
        active = confirmDropSheet,
        onYes = {
            confirmDropSheet = false
            viewModel.dropShow(item.show.ids.trakt)
        },
        onNo = { confirmDropSheet = false },
        title = stringResource(R.string.button_text_drop_show),
        message = stringResource(
            R.string.warning_prompt_drop_show,
            item.show.title,
        ),
    )
}

@Composable
private fun UpNextItemContextViewContent(
    item: ProgressShow,
    state: UpNextItemContextState,
    modifier: Modifier = Modifier,
    onAddWatched: () -> Unit = {},
    onRemoveWatchlist: () -> Unit = {},
) {
    Column(
        verticalArrangement = spacedBy(0.dp),
        modifier = modifier,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = spacedBy(12.dp),
        ) {
            HorizontalMediaCard(
                title = "",
                corner = 12.dp,
                width = TraktTheme.size.horizontalSmallMediaCardSize,
                containerImageUrl =
                    item.progress.nextEpisode.images?.getScreenshotUrl()
                        ?: item.show.images?.getFanartUrl(),
                modifier = Modifier.shadow(
                    elevation = 2.dp,
                    shape = RoundedCornerShape(12.dp),
                ),
            )

            Column(
                verticalArrangement = spacedBy(1.dp),
                modifier = Modifier.weight(1f),
            ) {
                Text(
                    text = item.show.title,
                    style = TraktTheme.typography.cardTitle.copy(fontSize = 13.sp),
                    color = TraktTheme.colors.textPrimary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )

                Text(
                    text = item.progress.nextEpisode.seasonEpisodeString(),
                    style = TraktTheme.typography.cardSubtitle.copy(fontSize = 13.sp),
                    color = TraktTheme.colors.textSecondary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }

        Spacer(
            modifier = Modifier
                .padding(top = 24.dp, bottom = 8.dp)
                .fillMaxWidth()
                .height(1.dp)
                .background(Shade910),
        )

        Column(
            verticalArrangement = spacedBy(TraktTheme.spacing.contextItemsSpace),
            modifier = Modifier
                .padding(top = 10.dp),
        ) {
            val isLoading =
                state.loadingWatched.isLoading ||
                    state.loadingDrop.isLoading

            GhostButton(
                enabled = !isLoading,
                loading = state.loadingWatched.isLoading,
                text = stringResource(R.string.button_text_mark_as_watched),
                iconSize = 20.dp,
                iconSpace = 16.dp,
                onClick = onAddWatched,
                icon = painterResource(R.drawable.ic_check_round),
                modifier = Modifier
                    .graphicsLayer {
                        translationX = -3.dp.toPx()
                    },
            )

            GhostButton(
                enabled = !isLoading,
                loading = state.loadingDrop.isLoading,
                text = stringResource(R.string.button_text_drop_show),
                onClick = onRemoveWatchlist,
                iconSize = 22.dp,
                iconSpace = 16.dp,
                icon = painterResource(R.drawable.ic_drop),
                modifier = Modifier
                    .graphicsLayer {
                        translationX = -5.dp.toPx()
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
            UpNextItemContextViewContent(
                state = UpNextItemContextState(),
                item = ProgressShow(
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
}
