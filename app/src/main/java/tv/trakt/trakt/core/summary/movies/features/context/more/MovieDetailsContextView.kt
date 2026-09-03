@file:OptIn(ExperimentalMaterial3Api::class)

package tv.trakt.trakt.core.summary.movies.features.context.more

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Box
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
import androidx.compose.ui.Alignment
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
import tv.trakt.trakt.common.helpers.extensions.openExternalAppLink
import tv.trakt.trakt.common.helpers.preview.PreviewData
import tv.trakt.trakt.common.helpers.streamingservices.StreamingServiceApp
import tv.trakt.trakt.common.model.Movie
import tv.trakt.trakt.common.model.streamings.StreamingService
import tv.trakt.trakt.core.summary.movies.features.context.more.MovieDetailsContextState.StreamingsState
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.components.buttons.GhostButton
import tv.trakt.trakt.ui.components.buttons.WatchNowButton
import tv.trakt.trakt.ui.theme.TraktTheme

@Composable
internal fun MovieDetailsContextView(
    movie: Movie,
    watched: Boolean,
    lists: Boolean,
    viewModel: MovieDetailsContextViewModel,
    modifier: Modifier = Modifier,
    onHistoryClick: (() -> Unit)? = null,
    onRemoveClick: (() -> Unit)? = null,
    onCheckClick: (() -> Unit)? = null,
    onListsClick: (() -> Unit)? = null,
    onCoverClick: (() -> Unit)? = null,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    MovieDetailsContextViewContent(
        movie = movie,
        watched = watched,
        lists = lists,
        state = state,
        onCheckClick = onCheckClick,
        onHistoryClick = onHistoryClick,
        onRemoveClick = onRemoveClick,
        onListsClick = onListsClick,
        onCoverClick = onCoverClick,
        modifier = modifier,
    )
}

@Composable
private fun MovieDetailsContextViewContent(
    movie: Movie,
    watched: Boolean,
    lists: Boolean,
    state: MovieDetailsContextState,
    modifier: Modifier = Modifier,
    onHistoryClick: (() -> Unit)? = null,
    onCheckClick: (() -> Unit)? = null,
    onRemoveClick: (() -> Unit)? = null,
    onListsClick: (() -> Unit)? = null,
    onCoverClick: (() -> Unit)? = null,
) {
    val context = LocalContext.current

    val isReleased = remember { movie.isReleased }
    val genresText = movie.genres.take(2)
        .map { stringResource(it.displayStringRes) }
        .joinToString(", ")

    Column(
        verticalArrangement = spacedBy(0.dp),
        modifier = modifier,
    ) {
        Row(
            verticalAlignment = CenterVertically,
            horizontalArrangement = spacedBy(24.dp),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column(
                verticalArrangement = spacedBy(2.dp),
                modifier = Modifier.weight(2f),
            ) {
                Text(
                    text = movie.title,
                    color = TraktTheme.colors.textPrimary,
                    style = TraktTheme.typography.heading3,
                    maxLines = 1,
                    overflow = Ellipsis,
                    autoSize = TextAutoSize.StepBased(
                        maxFontSize = TraktTheme.typography.heading3.fontSize,
                        minFontSize = 16.sp,
                        stepSize = 2.sp,
                    ),
                )

                Text(
                    text = "${movie.released?.year ?: movie.year}  •  $genresText",
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
                            openExternalAppLink(
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
                .background(TraktTheme.colors.separator)
                .fillMaxWidth()
                .height(1.dp),
        )

        ActionButtons(
            watched = watched,
            released = isReleased,
            lists = lists,
            watchOnlyOnce = state.user?.settings?.watchOnlyOnce,
            coverEnabled = !movie.images?.getFanartUrl().isNullOrBlank(),
            onCheckClick = onCheckClick ?: {},
            onHistoryClick = onHistoryClick ?: {},
            onRemoveClick = onRemoveClick ?: {},
            onListsClick = onListsClick ?: {},
            onCoverClick = onCoverClick ?: {},
            modifier = Modifier
                .padding(top = 14.dp),
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
            streamingState.loading -> stringResource(R.string.button_text_stream)
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
    lists: Boolean,
    released: Boolean,
    watchOnlyOnce: Boolean?,
    coverEnabled: Boolean,
    modifier: Modifier = Modifier,
    onCheckClick: () -> Unit,
    onHistoryClick: () -> Unit,
    onRemoveClick: () -> Unit,
    onListsClick: () -> Unit,
    onCoverClick: () -> Unit,
) {
    Column(
        verticalArrangement = spacedBy(TraktTheme.spacing.contextItemsSpace),
        modifier = modifier
            .graphicsLayer {
                translationX = -4.dp.toPx()
            },
    ) {
        if (!watched || watchOnlyOnce != true) {
            GhostButton(
                text = when {
                    !watched -> stringResource(R.string.button_text_track)
                    else -> stringResource(R.string.button_text_watch_again)
                },
                icon = when {
                    !watched -> painterResource(R.drawable.ic_check_2)
                    else -> painterResource(R.drawable.ic_check_double)
                },
                enabled = released,
                iconSize = 22.dp,
                iconSpace = 14.dp,
                onClick = onCheckClick,
                modifier = Modifier.graphicsLayer {
                    translationX = -4.dp.toPx()
                },
            )
        }

        if (watched) {
            GhostButton(
                text = stringResource(R.string.button_text_view_history),
                icon = painterResource(R.drawable.ic_calendar_check),
                iconSize = 24.dp,
                iconSpace = 14.5.dp,
                modifier = Modifier
                    .graphicsLayer {
                        translationX = -6.dp.toPx()
                    },
                onClick = onHistoryClick,
            )

            GhostButton(
                text = stringResource(R.string.button_text_remove_from_history),
                icon = painterResource(R.drawable.ic_close),
                iconSize = 22.dp,
                iconSpace = 15.5.dp,
                modifier = Modifier
                    .graphicsLayer {
                        translationX = -6.dp.toPx()
                    },
                onClick = onRemoveClick,
            )
        }

        if (lists) {
            GhostButton(
                text = stringResource(R.string.button_text_manage_lists),
                icon = painterResource(R.drawable.ic_lists_off),
                iconSize = 21.dp,
                iconSpace = 15.dp,
                modifier = Modifier
                    .graphicsLayer {
                        translationX = -4.dp.toPx()
                    },
                onClick = onListsClick,
            )
        }

        Box(
            contentAlignment = Alignment.CenterEnd,
            modifier = Modifier
                .graphicsLayer {
                    translationX = -5.dp.toPx()
                },
        ) {
            GhostButton(
                text = stringResource(R.string.button_text_set_cover_image),
                icon = painterResource(R.drawable.ic_image),
                iconSize = 22.dp,
                iconSpace = 15.dp,
                enabled = coverEnabled,
                onClick = onCoverClick,
            )
        }

//        GhostButton(
//            text = stringResource(R.string.button_text_share),
//            icon = painterResource(R.drawable.ic_share),
//            iconSize = 22.dp,
//            iconSpace = 15.dp,
//            modifier = Modifier
//                .graphicsLayer {
//                    translationX = -5.dp.toPx()
//                },
//            onClick = onShareClick,
//        )
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
    val state = MovieDetailsContextState(
        user = PreviewData.user1.copy(
            settings = PreviewData.user1.settings?.copy(
                watchOnlyOnce = false,
            ),
        ),
        streamings = StreamingsState(
            loading = false,
            service = StreamingService(
                source = "Hello",
                name = "Hello",
                logo = null,
                channel = "Hello",
                linkDirect = "Hello",
                uhd = false,
                color = null,
                country = "Hello",
                currency = null,
                purchasePrice = "Hello",
                rentPrice = "Hello",
            ),
        ),
    )

    TraktTheme {
        val previewHandler = AsyncImagePreviewHandler {
            ColorImage(Color.Blue.toArgb())
        }
        CompositionLocalProvider(LocalAsyncImagePreviewHandler provides previewHandler) {
            Column(
                verticalArrangement = spacedBy(64.dp),
                modifier = Modifier
                    .padding(24.dp),
            ) {
                MovieDetailsContextViewContent(
                    movie = PreviewData.movie1,
                    watched = false,
                    lists = false,
                    state = state,
                )

                MovieDetailsContextViewContent(
                    movie = PreviewData.movie1,
                    watched = true,
                    lists = true,
                    state = state,
                )
            }
        }
    }
}
