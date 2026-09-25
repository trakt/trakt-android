package tv.trakt.trakt.app.core.details.movie.views.header

import androidx.compose.foundation.focusGroup
import androidx.compose.foundation.layout.Arrangement.Absolute.spacedBy
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import kotlinx.collections.immutable.toImmutableList
import tv.trakt.trakt.app.Config.DEFAULT_PLEX_LOGO_URL
import tv.trakt.trakt.app.common.ui.buttons.OutlineButton
import tv.trakt.trakt.app.common.ui.buttons.PrimaryButton
import tv.trakt.trakt.app.common.ui.buttons.WatchNowButton
import tv.trakt.trakt.app.common.ui.menus.TvDropdownMenu
import tv.trakt.trakt.app.common.ui.menus.TvDropdownMenuItem
import tv.trakt.trakt.app.core.details.movie.MovieDetailsState
import tv.trakt.trakt.app.core.details.movie.MovieDetailsState.CollectionState
import tv.trakt.trakt.app.core.details.movie.MovieDetailsState.StreamingsState
import tv.trakt.trakt.app.core.details.movie.usecases.streamings.GetPlexUseCase
import tv.trakt.trakt.app.core.details.ui.dateselection.DateSelectionMenu
import tv.trakt.trakt.app.core.player.plex.TvPlexPlayerActivity
import tv.trakt.trakt.app.helpers.extensions.requestSafeFocus
import tv.trakt.trakt.app.ui.theme.TraktTheme
import tv.trakt.trakt.common.helpers.extensions.openPlexLink
import tv.trakt.trakt.common.helpers.extensions.openWatchNowLink
import tv.trakt.trakt.common.model.DateSelectionResult
import tv.trakt.trakt.common.model.ExtraVideo
import tv.trakt.trakt.common.model.MediaType
import tv.trakt.trakt.common.ui.theme.colors.Purple500
import tv.trakt.trakt.resources.R
import java.time.ZonedDateTime

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
internal fun MovieActionButtons(
    movieState: MovieDetailsState,
    onHistoryClick: (DateSelectionResult) -> Unit,
    onRemoveHistoryClick: () -> Unit,
    onWatchlistClick: () -> Unit,
    onTrailerClick: (String) -> Unit,
    onStreamingLongClick: () -> Unit,
    onDropMovieClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val uriHandler = LocalUriHandler.current
    val buttonsWidth = TraktTheme.size.detailsPosterSize * 0.666F

    val collectionState = movieState.movieCollection
    val streamingState = movieState.movieStreamings

    val trailerUrl = remember(movieState.movieVideos) {
        movieState.movieVideos?.firstOrNull { it.type == "trailer" }?.url
    }

    Column(
        verticalArrangement = spacedBy(8.dp),
        modifier = modifier.width(buttonsWidth),
    ) {
        StreamingButton(
            streamingState = streamingState,
            onLongClick = onStreamingLongClick,
            onClick = {
                if (streamingState.plex) {
                    if (streamingState.plexStream?.primaryUrl.isNullOrBlank()) {
                        openPlexLink(
                            uriHandler = uriHandler,
                            slug = streamingState.slug?.value,
                            type = "movie",
                        )
                    } else {
                        val intent = TvPlexPlayerActivity.createIntent(
                            context = context,
                            mediaId = movieState.movieDetails?.ids?.trakt ?: return@StreamingButton,
                            mediaType = MediaType.Movie,
                            primaryVideoUrl = streamingState.plexStream.primaryUrl,
                            secondaryVideoUrls = streamingState.plexStream.secondaryUrls,
                            videoTitle = movieState.movieDetails.title,
                            videoSubtitle = movieState.movieDetails.yearString,
                            videoProgress = streamingState.plexStream.progress,
                        )
                        context.startActivity(intent)
                    }
                } else {
                    openWatchNowLink(
                        context = context,
                        uriHandler = uriHandler,
                        link = streamingState.service?.linkDirect,
                    )
                }
            },
            modifier = Modifier.fillMaxWidth(),
        )

        val isHistory = remember(collectionState.isHistory) { collectionState.isHistory }
        MarkAsWatchedButton(
            isHistory = isHistory,
            collectionState = collectionState,
            onHistoryClick = onHistoryClick,
            onRemoveHistoryClick = onRemoveHistoryClick,
            modifier = Modifier.fillMaxWidth(),
        )

        val watchlistFocusRequester = remember { FocusRequester() }
        Row(
            horizontalArrangement = spacedBy(6.dp),
            verticalAlignment = CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .focusProperties {
                    onEnter = { watchlistFocusRequester.requestSafeFocus() }
                }
                .focusGroup(),
        ) {
            WatchlistButton(
                collectionState = collectionState,
                onClick = onWatchlistClick,
                modifier = Modifier
                    .weight(1F)
                    .focusRequester(watchlistFocusRequester),
            )

            OutlineButton(
                icon = painterResource(R.drawable.ic_trailer),
                iconSize = 20.dp,
                enabled = trailerUrl != null && !collectionState.isLoading,
                onClick = { trailerUrl?.let(onTrailerClick) },
                modifier = Modifier.weight(1F),
            )

            MoreButton(
                movieState = movieState,
                isHistory = isHistory,
                onHistoryClick = onHistoryClick,
                onRemoveHistoryClick = onRemoveHistoryClick,
                onStreamOnPlexClick = {
                    openPlexLink(
                        uriHandler = uriHandler,
                        slug = streamingState.slug?.value,
                        type = "movie",
                    )
                },
                onWhereToWatchClick = onStreamingLongClick,
                onDropMovieClick = onDropMovieClick,
                modifier = Modifier.weight(1F),
            )
        }
    }
}

@Composable
private fun WatchlistButton(
    collectionState: CollectionState,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val isWatchlist = remember(collectionState.isWatchlist) { collectionState.isWatchlist }
    OutlineButton(
        icon = painterResource(if (isWatchlist) R.drawable.ic_bookmark_on else R.drawable.ic_bookmark_off),
        iconSize = 22.dp,
        onClick = onClick,
        enabled = !collectionState.isLoading,
        loading = collectionState.isWatchlistLoading,
        modifier = modifier,
    )
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun MarkAsWatchedButton(
    isHistory: Boolean,
    collectionState: CollectionState,
    onHistoryClick: (DateSelectionResult) -> Unit,
    onRemoveHistoryClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val menuVisible = remember { mutableStateOf(false) }

    Box(modifier = modifier) {
        PrimaryButton(
            text = stringResource(
                if (isHistory) R.string.tag_text_watched else R.string.button_text_mark_as_watched,
            ),
            icon = painterResource(if (isHistory) R.drawable.ic_check_double else R.drawable.ic_check_2),
            onClick = {
                if (isHistory) {
                    onRemoveHistoryClick()
                } else {
                    menuVisible.value = true
                }
            },
            containerColor = if (isHistory) Color.White else Purple500,
            contentColor = if (isHistory) Color.Black else Color.White,
            borderColor = if (isHistory) Purple500 else Color.White,
            enabled = !collectionState.isLoading,
            loading = collectionState.isHistoryLoading,
            modifier = Modifier.fillMaxWidth(),
        )

        Box(
            modifier = Modifier.align(Alignment.CenterEnd),
        ) {
            DateSelectionMenu(
                expanded = menuVisible.value,
                onDismissRequest = { menuVisible.value = false },
                onSelect = onHistoryClick,
            )
        }
    }
}

@Composable
private fun StreamingButton(
    streamingState: StreamingsState,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current

    val plex = streamingState.plex
    val plexStream = !streamingState.plexStream?.primaryUrl.isNullOrBlank()
    val plexStreamProgress = streamingState.plexStream?.progress ?: 0F
    val service = streamingState.service
    val loading = streamingState.loading
    val directLink = service?.linkDirect

    WatchNowButton(
        text = when {
            plex && plexStream -> when {
                plexStreamProgress > 0F -> stringResource(R.string.button_text_player_resume)
                else -> stringResource(R.string.button_text_player_play_now)
            }
            loading || !directLink.isNullOrBlank() || plex -> stringResource(R.string.button_text_stream)
            streamingState.noServices -> stringResource(R.string.button_text_no_services)
            else -> stringResource(R.string.button_text_where_to_watch)
        },
        secondaryText = when {
            !loading && (plex || directLink != null) && streamingState.info != null -> {
                streamingState.info.get(context)
            }
            else -> {
                null
            }
        },
        name = when {
            plex && plexStream -> ""
            plex -> "Plex"
            directLink != null -> service.name
            else -> ""
        },
        logo = when {
            plex && plexStream -> null
            plex -> DEFAULT_PLEX_LOGO_URL
            directLink != null -> service.logo
            else -> null
        },
        enabled = !loading && !streamingState.noServices,
        loading = loading,
        containerColor = when {
            plex && plexStream -> TraktTheme.colors.primaryButtonContainer
            plex -> Color(0xFFE8AE0A)
            (service?.color != null) -> service.color ?: TraktTheme.colors.primaryButtonContainer
            else -> TraktTheme.colors.primaryButtonContainerDisabled
        },
        onLongClick = onLongClick,
        onClick = {
            if (directLink == null && !plex) {
                onLongClick()
                return@WatchNowButton
            }
            onClick()
        },
        modifier = modifier,
    )
}

private data class MoreMenuItem(
    val text: String,
    val icon: Painter? = null,
    val enabled: Boolean = true,
    val onClick: () -> Unit,
)

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun MoreButton(
    movieState: MovieDetailsState,
    isHistory: Boolean,
    onHistoryClick: (DateSelectionResult) -> Unit,
    onRemoveHistoryClick: () -> Unit,
    onStreamOnPlexClick: () -> Unit,
    onWhereToWatchClick: () -> Unit,
    onDropMovieClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val collectionState = movieState.movieCollection
    val streamingState = movieState.movieStreamings
    val plexStream = streamingState.plex && !streamingState.plexStream?.primaryUrl.isNullOrBlank()
    val plexProgress = streamingState.plexStream?.progress ?: 0F
    val watchAgainEnabled = movieState.user?.settings?.watchOnlyOnce != true

    val menuVisible = remember { mutableStateOf(false) }
    val dateMenuVisible = remember { mutableStateOf(false) }

    val items = buildList {
        if (plexStream) {
            add(
                MoreMenuItem(
                    text = "${stringResource(R.string.button_text_stream)} Plex",
                    onClick = onStreamOnPlexClick,
                ),
            )
        }
        add(
            MoreMenuItem(
                text = stringResource(R.string.button_text_where_to_watch),
                icon = painterResource(R.drawable.ic_play),
                onClick = onWhereToWatchClick,
            ),
        )
        if (plexStream && plexProgress > 0F) {
            add(
                MoreMenuItem(
                    text = stringResource(R.string.button_text_drop_movie).uppercase(),
                    onClick = onDropMovieClick,
                ),
            )
        }
        if (isHistory) {
            add(
                MoreMenuItem(
                    text = stringResource(R.string.button_text_watch_again),
                    icon = painterResource(R.drawable.ic_check_double),
                    enabled = watchAgainEnabled,
                    onClick = { dateMenuVisible.value = true },
                ),
            )
            add(
                MoreMenuItem(
                    text = stringResource(R.string.button_text_remove_from_history),
                    icon = painterResource(R.drawable.ic_trash),
                    onClick = onRemoveHistoryClick,
                ),
            )
        }
    }.toImmutableList()

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier,
    ) {
        OutlineButton(
            icon = painterResource(R.drawable.ic_more_vertical),
            iconSize = 17.dp,
            onClick = { menuVisible.value = true },
            enabled = !collectionState.isLoading && !streamingState.loading,
            modifier = Modifier.fillMaxWidth(),
        )

        TvDropdownMenu(
            visible = menuVisible.value,
            onDismiss = { menuVisible.value = false },
        ) {
            var focusedIndex by remember { mutableIntStateOf(0) }

            items.forEachIndexed { index, item ->
                TvDropdownMenuItem(
                    text = item.text,
                    icon = item.icon,
                    enabled = item.enabled,
                    focused = focusedIndex == index,
                    onFocus = { focusedIndex = index },
                    onClick = {
                        menuVisible.value = false
                        item.onClick()
                    },
                )
            }
        }

        DateSelectionMenu(
            expanded = dateMenuVisible.value,
            onDismissRequest = { dateMenuVisible.value = false },
            onSelect = onHistoryClick,
        )
    }
}

@Preview
@Composable
private fun Preview1() {
    TraktTheme {
        MovieActionButtons(
            movieState = MovieDetailsState(
                movieCollection = CollectionState(
                    isHistory = true,
                    isWatchlist = true,
                ),
                movieStreamings = StreamingsState(
                    plex = true,
                    plexStream = GetPlexUseCase.PlexStreamResult(
                        primaryUrl = "https://example.com/stream",
                        secondaryUrls = listOf("https://example.com/stream2"),
                        progress = 50F,
                    ),
                ),
                movieVideos = listOf(
                    ExtraVideo(
                        title = "Trailer",
                        url = "https://youtube.com/watch?v=4mdAbk4dXXY",
                        site = "youtube",
                        type = "trailer",
                        official = true,
                        publishedAt = ZonedDateTime.now(),
                    ),
                ).toImmutableList(),
            ),
            onHistoryClick = {},
            onRemoveHistoryClick = {},
            onWatchlistClick = {},
            onTrailerClick = {},
            onStreamingLongClick = {},
            onDropMovieClick = {},
        )
    }
}

@Preview
@Composable
private fun Preview2(
    @PreviewParameter(PreviewParameters::class) collectionState: CollectionState,
) {
    TraktTheme {
        MovieActionButtons(
            MovieDetailsState(
                movieCollection = collectionState,
                movieStreamings = StreamingsState(
                    plex = true,
                    plexStream = null,
                ),
            ),
            onHistoryClick = {},
            onRemoveHistoryClick = {},
            onWatchlistClick = {},
            onTrailerClick = {},
            onStreamingLongClick = {},
            onDropMovieClick = {},
        )
    }
}

private class PreviewParameters : PreviewParameterProvider<CollectionState> {
    override val values = sequenceOf(
        CollectionState(),
        CollectionState(
            isWatchlistLoading = true,
            isHistoryLoading = true,
        ),
        CollectionState(
            isHistory = true,
            isWatchlist = true,
        ),
    )
}
