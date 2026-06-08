package tv.trakt.trakt.app.core.details.movie.views.header

import androidx.compose.foundation.layout.Arrangement.Absolute.spacedBy
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import tv.trakt.trakt.app.Config.DEFAULT_PLEX_LOGO_URL
import tv.trakt.trakt.app.common.ui.buttons.IconButton
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
import tv.trakt.trakt.app.ui.theme.TraktTheme
import tv.trakt.trakt.common.helpers.extensions.openPlexLink
import tv.trakt.trakt.common.helpers.extensions.openWatchNowLink
import tv.trakt.trakt.common.model.DateSelectionResult
import tv.trakt.trakt.common.model.MediaType
import tv.trakt.trakt.common.ui.theme.colors.Blue50
import tv.trakt.trakt.common.ui.theme.colors.Blue500
import tv.trakt.trakt.common.ui.theme.colors.Purple50
import tv.trakt.trakt.common.ui.theme.colors.Purple500
import tv.trakt.trakt.resources.R

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
internal fun MovieActionButtons(
    movieState: MovieDetailsState,
    onHistoryClick: (DateSelectionResult) -> Unit,
    onRemoveHistoryClick: () -> Unit,
    onWatchlistClick: () -> Unit,
    onStreamingLongClick: () -> Unit,
    onDropMovieClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val uriHandler = LocalUriHandler.current
    val buttonsWidth = TraktTheme.size.detailsPosterSize * 0.666F

    val collectionState = movieState.movieCollection
    val streamingState = movieState.movieStreamings

    Column(
        verticalArrangement = spacedBy(8.dp),
        modifier = modifier.width(buttonsWidth),
    ) {
        Row(
            horizontalArrangement = spacedBy(6.dp),
            verticalAlignment = CenterVertically,
            modifier = Modifier.fillMaxWidth(),
        ) {
            StreamingButton(
                streamingState = movieState.movieStreamings,
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
                                mediaType = MediaType.MOVIE,
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
                modifier = Modifier.weight(1f, false),
            )

            if (streamingState.plex ||
                !streamingState.plexStream?.primaryUrl.isNullOrBlank() ||
                !streamingState.service?.linkDirect.isNullOrBlank()
            ) {
                StreamingDropDownButton(
                    enabled = !streamingState.loading,
                    streamingState = movieState.movieStreamings,
                    onStreamOnPlexClick = {
                        openPlexLink(
                            uriHandler = uriHandler,
                            slug = streamingState.slug?.value,
                            type = "movie",
                        )
                    },
                    onWhereToWatchClick = onStreamingLongClick,
                    onDropMovieClick = onDropMovieClick,
                )
            }
        }

        val isHistory = remember(collectionState.isHistory) { collectionState.isHistory }
        Row(
            horizontalArrangement = spacedBy(6.dp),
            verticalAlignment = CenterVertically,
            modifier = Modifier.fillMaxWidth(),
        ) {
            val dateMenuVisible = remember { mutableStateOf(false) }

            MarkAsWatchedButton(
                isHistory = isHistory,
                collectionState = collectionState,
                onHistoryClick = onHistoryClick,
                onRemoveHistoryClick = onRemoveHistoryClick,
                modifier = Modifier.weight(1f, false),
            )

            if (!collectionState.isLoading && collectionState.isHistory) {
                Box(
                    contentAlignment = Alignment.Center,
                ) {
                    HistoryDropDownButton(
                        enabled = !collectionState.isLoading,
                        watchAgainEnabled = movieState.user?.settings?.watchOnlyOnce != true,
                        onWatchAgainClick = { dateMenuVisible.value = true },
                        onRemoveFromHistoryClick = onRemoveHistoryClick,
                    )

                    DateSelectionMenu(
                        expanded = dateMenuVisible.value,
                        onDismissRequest = { dateMenuVisible.value = false },
                        onSelect = onHistoryClick,
                    )
                }
            }
        }

        val isWatchlist = remember(collectionState.isWatchlist) { collectionState.isWatchlist }
        PrimaryButton(
            text = stringResource(
                if (isWatchlist) R.string.button_text_watchlist else R.string.button_text_watchlist,
            ),
            icon = painterResource(if (isWatchlist) R.drawable.ic_minus else R.drawable.ic_plus),
            onClick = onWatchlistClick,
            containerColor = if (isWatchlist) Blue500 else Blue50,
            contentColor = if (isWatchlist) Color.White else Blue500,
            borderColor = if (isWatchlist) Color.White else Blue500,
            enabled = !collectionState.isLoading,
            loading = collectionState.isWatchlistLoading,
        )
    }
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

    Box(modifier = modifier.fillMaxWidth()) {
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
            containerColor = if (isHistory) Purple500 else Purple50,
            contentColor = if (isHistory) Color.White else Purple500,
            borderColor = if (isHistory) Color.White else Purple500,
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

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun StreamingDropDownButton(
    streamingState: StreamingsState,
    onStreamOnPlexClick: () -> Unit,
    onWhereToWatchClick: () -> Unit,
    onDropMovieClick: () -> Unit,
    enabled: Boolean,
    modifier: Modifier = Modifier,
) {
    val plex = streamingState.plex
    val plexStream = !streamingState.plexStream?.primaryUrl.isNullOrBlank()

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier,
    ) {
        val menuVisible = remember { mutableStateOf(false) }
        IconButton(
            icon = painterResource(R.drawable.ic_more_vertical),
            iconSize = 14.dp,
            size = 32.dp,
            onClick = {
                when {
                    plex && plexStream -> menuVisible.value = true
                    else -> onWhereToWatchClick()
                }
            },
            containerColor = Color.Transparent,
            contentColor = TraktTheme.colors.primaryButtonContent,
            borderColor = Color.White,
            enabled = enabled,
            modifier = Modifier.height(42.dp),
        )

        TvDropdownMenu(
            visible = menuVisible.value,
            onDismiss = { menuVisible.value = false },
        ) {
            var focusedIndex by remember { mutableIntStateOf(0) }

            TvDropdownMenuItem(
                text = "${stringResource(R.string.button_text_stream)} Plex",
                focused = focusedIndex == 0,
                onFocus = { focusedIndex = 0 },
                onClick = {
                    menuVisible.value = false
                    onStreamOnPlexClick()
                },
            )

            TvDropdownMenuItem(
                text = stringResource(R.string.button_text_where_to_watch),
                focused = focusedIndex == 1,
                onFocus = { focusedIndex = 1 },
                onClick = {
                    menuVisible.value = false
                    onWhereToWatchClick()
                },
            )

            if ((streamingState.plexStream?.progress ?: 0F) > 0F) {
                TvDropdownMenuItem(
                    text = stringResource(R.string.button_text_drop_movie).uppercase(),
                    focused = focusedIndex == 2,
                    onFocus = { focusedIndex = 2 },
                    onClick = {
                        menuVisible.value = false
                        onDropMovieClick()
                    },
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun HistoryDropDownButton(
    enabled: Boolean,
    watchAgainEnabled: Boolean,
    onWatchAgainClick: () -> Unit,
    onRemoveFromHistoryClick: () -> Unit,
) {
    val menuVisible = remember { mutableStateOf(false) }
    IconButton(
        icon = painterResource(R.drawable.ic_more_vertical),
        iconSize = 14.dp,
        size = 32.dp,
        onClick = { menuVisible.value = true },
        containerColor = Color.Transparent,
        contentColor = TraktTheme.colors.primaryButtonContent,
        borderColor = Color.White,
        enabled = enabled,
        modifier = Modifier.height(42.dp),
    )

    TvDropdownMenu(
        visible = menuVisible.value,
        onDismiss = { menuVisible.value = false },
    ) {
        var focusedIndex by remember { mutableIntStateOf(0) }

        TvDropdownMenuItem(
            text = stringResource(R.string.button_text_watch_again),
            icon = painterResource(R.drawable.ic_check_double),
            enabled = watchAgainEnabled,
            focused = focusedIndex == 0,
            onFocus = { focusedIndex = 0 },
            onClick = {
                menuVisible.value = false
                onWatchAgainClick()
            },
        )

        TvDropdownMenuItem(
            text = stringResource(R.string.button_text_remove_from_history),
            icon = painterResource(R.drawable.ic_trash),
            focused = focusedIndex == 1,
            onFocus = { focusedIndex = 1 },
            onClick = {
                menuVisible.value = false
                onRemoveFromHistoryClick()
            },
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
            ),
            onHistoryClick = {},
            onRemoveHistoryClick = {},
            onWatchlistClick = {},
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
