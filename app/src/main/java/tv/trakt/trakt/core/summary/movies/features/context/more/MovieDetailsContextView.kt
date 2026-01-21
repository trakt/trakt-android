@file:OptIn(ExperimentalMaterial3Api::class)

package tv.trakt.trakt.core.summary.movies.features.context.more

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
import tv.trakt.trakt.common.helpers.extensions.isTodayOrBefore
import tv.trakt.trakt.common.helpers.extensions.openExternalAppLink
import tv.trakt.trakt.common.helpers.preview.PreviewData
import tv.trakt.trakt.common.helpers.streamingservices.StreamingServiceApp
import tv.trakt.trakt.common.model.Movie
import tv.trakt.trakt.common.model.streamings.StreamingService
import tv.trakt.trakt.common.ui.theme.colors.Shade910
import tv.trakt.trakt.core.summary.movies.features.context.more.MovieDetailsContextState.StreamingsState
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.components.buttons.GhostButton
import tv.trakt.trakt.ui.components.buttons.WatchNowButton
import tv.trakt.trakt.ui.theme.TraktTheme

@Composable
internal fun MovieDetailsContextView(
    movie: Movie,
    watched: Boolean,
    viewModel: MovieDetailsContextViewModel,
    modifier: Modifier = Modifier,
    onShareClick: (() -> Unit)? = null,
    onRemoveClick: (() -> Unit)? = null,
    onCheckClick: (() -> Unit)? = null,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    MovieDetailsContextViewContent(
        movie = movie,
        watched = watched,
        state = state,
        onCheckClick = onCheckClick,
        onShareClick = onShareClick,
        onRemoveClick = onRemoveClick,
        modifier = modifier,
    )
}

@Composable
private fun MovieDetailsContextViewContent(
    movie: Movie,
    watched: Boolean,
    state: MovieDetailsContextState,
    modifier: Modifier = Modifier,
    onCheckClick: (() -> Unit)? = null,
    onRemoveClick: (() -> Unit)? = null,
    onShareClick: (() -> Unit)? = null,
) {
    val context = LocalContext.current

    val isReleased = remember {
        movie.released?.isTodayOrBefore() ?: false
    }

    val genresText = remember(movie.genres) {
        movie.genres.take(2).joinToString(" / ") { genre ->
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
                .background(Shade910)
                .fillMaxWidth()
                .height(1.dp),
        )

        ActionButtons(
            watched = watched,
            released = isReleased,
            watchOnlyOnce = state.user?.settings?.watchOnlyOnce,
            onCheckClick = onCheckClick ?: {},
            onRemoveClick = onRemoveClick ?: {},
            onShareClick = onShareClick ?: {},
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
    released: Boolean,
    watchOnlyOnce: Boolean?,
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
        if (!watched) {
            GhostButton(
                text = stringResource(R.string.button_text_mark_as_watched),
                icon = painterResource(R.drawable.ic_check),
                enabled = released,
                iconSize = 22.dp,
                iconSpace = 16.dp,
                onClick = onCheckClick,
                modifier = Modifier.graphicsLayer {
                    translationX = -4.dp.toPx()
                },
            )
        } else if (watchOnlyOnce != true) {
            GhostButton(
                text = stringResource(R.string.button_text_watch_again),
                icon = painterResource(R.drawable.ic_check_double),
                enabled = released,
                iconSize = 22.dp,
                iconSpace = 16.dp,
                modifier = Modifier.graphicsLayer {
                    translationX = -4.dp.toPx()
                },
                onClick = onCheckClick,
            )
        }

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
                    state = state,
                )

                MovieDetailsContextViewContent(
                    movie = PreviewData.movie1,
                    watched = true,
                    state = state,
                )
            }
        }
    }
}
