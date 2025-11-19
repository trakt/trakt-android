@file:OptIn(ExperimentalMaterial3Api::class)

package tv.trakt.trakt.core.summary.shows.features.context.more

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow.Companion.Ellipsis
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.ColorImage
import coil3.annotation.ExperimentalCoilApi
import coil3.compose.AsyncImagePreviewHandler
import coil3.compose.LocalAsyncImagePreviewHandler
import tv.trakt.trakt.common.helpers.extensions.isNowOrBefore
import tv.trakt.trakt.common.helpers.preview.PreviewData
import tv.trakt.trakt.common.helpers.streamingservices.StreamingServiceApp
import tv.trakt.trakt.common.helpers.streamingservices.StreamingServiceLink
import tv.trakt.trakt.common.model.Show
import tv.trakt.trakt.common.model.streamings.StreamingService
import tv.trakt.trakt.common.ui.theme.colors.Shade910
import tv.trakt.trakt.core.summary.shows.features.context.more.ShowDetailsContextState.StreamingsState
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.components.buttons.GhostButton
import tv.trakt.trakt.ui.components.buttons.WatchNowButton
import tv.trakt.trakt.ui.theme.TraktTheme

@Composable
internal fun ShowDetailsContextView(
    show: Show,
    watched: Boolean,
    viewModel: ShowDetailsContextViewModel,
    modifier: Modifier = Modifier,
    onShareClick: (() -> Unit)? = null,
    onRemoveClick: (() -> Unit)? = null,
    onCheckClick: (() -> Unit)? = null,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    ShowDetailsContextViewContent(
        show = show,
        watched = watched,
        state = state,
        onShareClick = onShareClick,
        onRemoveClick = onRemoveClick,
        onCheckClick = onCheckClick,
        modifier = modifier,
    )
}

@Composable
private fun ShowDetailsContextViewContent(
    show: Show,
    watched: Boolean,
    state: ShowDetailsContextState,
    modifier: Modifier = Modifier,
    onCheckClick: (() -> Unit)? = null,
    onRemoveClick: (() -> Unit)? = null,
    onShareClick: (() -> Unit)? = null,
) {
    val context = LocalContext.current

    val isReleased = remember {
        show.released?.isNowOrBefore() ?: false
    }

    val genresText = remember(show.genres) {
        show.genres.take(2).joinToString(" / ") { genre ->
            genre.replaceFirstChar {
                it.uppercaseChar()
            }
        }
    }

    Column(
        verticalArrangement = spacedBy(0.dp),
        modifier = modifier,
    ) {
        Row(
            verticalAlignment = CenterVertically,
            horizontalArrangement = spacedBy(16.dp),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column(
                verticalArrangement = spacedBy(2.dp),
                modifier = Modifier.weight(2f),
            ) {
                Text(
                    text = show.title,
                    color = TraktTheme.colors.textPrimary,
                    style = TraktTheme.typography.heading2,
                    maxLines = 1,
                    overflow = Ellipsis,
                    autoSize = TextAutoSize.StepBased(
                        maxFontSize = TraktTheme.typography.heading2.fontSize,
                        minFontSize = 16.sp,
                        stepSize = 2.sp,
                    ),
                )

                Text(
                    text = "${show.released?.year ?: show.year}  •  $genresText",
                    color = TraktTheme.colors.textSecondary,
                    style = TraktTheme.typography.paragraphSmaller,
                    maxLines = 1,
                    overflow = Ellipsis,
                )
            }

            if (isReleased && state.user != null) {
                WatchButton(
                    streamingState = state.streamings,
                    onClick = {
                        state.streamings.service?.let { service ->
                            StreamingServiceLink.openApp(
                                packageId = StreamingServiceApp.findFromSource(service.source)?.packageId,
                                packageName = service.source,
                                uri = service.linkDirect?.toUri(),
                                context = context,
                            )
                        }
                    },
                    modifier = Modifier
                        .weight(1f)
                        .padding(top = 4.dp),
                )
            }
        }

        Spacer(
            modifier = Modifier
                .padding(top = 22.dp)
                .background(Shade910)
                .fillMaxWidth()
                .height(1.dp),
        )

        ActionButtons(
            watched = watched,
            onCheckClick = onCheckClick ?: {},
            onRemoveClick = onRemoveClick ?: {},
            onShareClick = onShareClick ?: {},
            modifier = Modifier
                .padding(top = 12.dp),
        )
    }
}

@Composable
private fun WatchButton(
    streamingState: StreamingsState,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val service = streamingState.service
    val loading = streamingState.loading
    val directLink = service?.linkDirect

    WatchNowButton(
        text = when {
            streamingState.loading -> stringResource(R.string.button_text_stream_on)
            streamingState.noServices -> stringResource(R.string.button_text_no_services)
            directLink != null -> service.name
            else -> ""
        },
        textStyle = when {
            streamingState.loading -> TraktTheme.typography.buttonPrimary.copy(fontSize = 12.sp)
            streamingState.noServices -> TraktTheme.typography.buttonPrimary.copy(fontSize = 12.sp)
            else -> TraktTheme.typography.buttonPrimary
        },
        logo = when {
            directLink != null -> service.logo
            else -> null
        },
        enabled = !loading && !streamingState.noServices,
        loading = loading,
        containerColor = when {
            (service?.color != null) -> service.color ?: TraktTheme.colors.primaryButtonContainer
            else -> TraktTheme.colors.primaryButtonContainerDisabled
        },
        height = 40.dp,
        corner = 12.dp,
        onClick = onClick,
        modifier = modifier,
    )
}

@Composable
private fun ActionButtons(
    watched: Boolean,
    modifier: Modifier = Modifier,
    onCheckClick: () -> Unit,
    onShareClick: () -> Unit,
    onRemoveClick: () -> Unit,
) {
    Column(
        verticalArrangement = spacedBy(TraktTheme.spacing.contextItemsSpace),
        modifier = modifier
            .graphicsLayer {
                translationX = -4.dp.toPx()
            },
    ) {
        GhostButton(
            text = stringResource(
                when {
                    watched -> R.string.button_text_watch_again
                    else -> R.string.button_text_mark_as_watched
                },
            ),
            icon = painterResource(
                when {
                    watched -> R.drawable.ic_check_double
                    else -> R.drawable.ic_check
                },
            ),
            iconSize = 22.dp,
            iconSpace = 16.dp,
            modifier = Modifier
                .graphicsLayer {
                    translationX = -4.dp.toPx()
                },
            onClick = onCheckClick,
        )

        if (watched) {
            GhostButton(
                text = stringResource(R.string.button_text_remove_from_history),
                icon = painterResource(R.drawable.ic_close),
                iconSize = 22.dp,
                iconSpace = 16.dp,
                modifier = Modifier
                    .graphicsLayer {
                        translationX = -4.dp.toPx()
                    },
                onClick = onRemoveClick,
            )
        }

        GhostButton(
            text = stringResource(R.string.button_text_share),
            icon = painterResource(R.drawable.ic_share),
            iconSize = 22.dp,
            iconSpace = 16.dp,
            modifier = Modifier
                .graphicsLayer {
                    translationX = -4.dp.toPx()
                },
            onClick = onShareClick,
        )
    }
}

@OptIn(ExperimentalCoilApi::class)
@Preview(
    device = "id:pixel_5",
    showBackground = true,
    backgroundColor = 0xFF212427,
)
@Composable
private fun Preview() {
    TraktTheme {
        val previewHandler = AsyncImagePreviewHandler {
            ColorImage(Color.Blue.toArgb())
        }
        CompositionLocalProvider(LocalAsyncImagePreviewHandler provides previewHandler) {
            ShowDetailsContextViewContent(
                show = PreviewData.show1,
                watched = true,
                state = ShowDetailsContextState(
                    streamings = StreamingsState(
                        loading = false,
                        service = StreamingService(
                            source = "Hello",
                            name = "Hello",
                            logo = null,
                            channel = "Hello",
                            linkDirect = "Hello",
                            linkAndroid = "Hello",
                            uhd = false,
                            color = null,
                            country = "Hello",
                            currency = null,
                            purchasePrice = "Hello",
                            rentPrice = "Hello",
                        ),
                    ),
                ),
            )
        }
    }
}
