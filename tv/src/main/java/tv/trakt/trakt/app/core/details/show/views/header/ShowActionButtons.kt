package tv.trakt.trakt.app.core.details.show.views.header

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
import tv.trakt.trakt.app.core.details.show.ShowDetailsState.CollectionState
import tv.trakt.trakt.app.core.details.show.ShowDetailsState.StreamingsState
import tv.trakt.trakt.app.core.details.ui.dateselection.DateSelectionMenu
import tv.trakt.trakt.app.helpers.extensions.requestSafeFocus
import tv.trakt.trakt.app.ui.theme.TraktTheme
import tv.trakt.trakt.common.helpers.extensions.openPlexLink
import tv.trakt.trakt.common.helpers.extensions.openWatchNowLink
import tv.trakt.trakt.common.model.DateSelectionResult
import tv.trakt.trakt.common.ui.theme.colors.Purple50
import tv.trakt.trakt.common.ui.theme.colors.Purple500
import tv.trakt.trakt.resources.R

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
internal fun ShowActionButtons(
    streamingState: StreamingsState,
    collectionState: CollectionState,
    watchAgainEnabled: Boolean,
    trailerUrl: String?,
    onHistoryClick: (DateSelectionResult) -> Unit,
    onRemoveHistoryClick: () -> Unit,
    onWatchlistClick: () -> Unit,
    onTrailerClick: (String) -> Unit,
    onStreamingLongClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val uriHandler = LocalUriHandler.current
    val buttonsWidth = TraktTheme.size.detailsPosterSize * 0.666F

    Column(
        verticalArrangement = spacedBy(8.dp),
        modifier = modifier.width(buttonsWidth),
    ) {
        WatchButton(
            streamingState = streamingState,
            onLongClick = onStreamingLongClick,
            onClick = {
                if (streamingState.plex) {
                    openPlexLink(
                        uriHandler = uriHandler,
                        slug = streamingState.slug?.value,
                        type = "show",
                    )
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

        val isWatched = remember(collectionState.isWatched) { collectionState.isWatched }
        val isAllWatched = remember(collectionState.isAllWatched) { collectionState.isAllWatched }
        MarkAsWatchedButton(
            isWatched = isWatched,
            isAllWatched = isAllWatched,
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
                streamingState = streamingState,
                collectionState = collectionState,
                isWatched = isWatched,
                watchAgainEnabled = watchAgainEnabled,
                onHistoryClick = onHistoryClick,
                onRemoveHistoryClick = onRemoveHistoryClick,
                onWhereToWatchClick = onStreamingLongClick,
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

@Composable
private fun MarkAsWatchedButton(
    isWatched: Boolean,
    isAllWatched: Boolean,
    collectionState: CollectionState,
    onHistoryClick: (DateSelectionResult) -> Unit,
    onRemoveHistoryClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val menuVisible = remember { mutableStateOf(false) }

    Box(modifier = modifier) {
        PrimaryButton(
            text = when {
                collectionState.isStarted -> {
                    stringResource(R.string.tag_text_started)
                }
                collectionState.fullWatchesCount > 1 -> {
                    "${stringResource(R.string.tag_text_watched)} • ${collectionState.fullWatchesCount}"
                }
                isWatched -> {
                    stringResource(R.string.tag_text_watched)
                }
                else -> {
                    stringResource(R.string.button_text_mark_as_watched)
                }
            },
            icon = painterResource(
                if (isAllWatched) R.drawable.ic_check_double else R.drawable.ic_check_2,
            ),
            onClick = {
                if (isWatched) {
                    onRemoveHistoryClick()
                } else {
                    menuVisible.value = true
                }
            },
            containerColor = if (!isWatched) Purple50 else Purple500,
            contentColor = if (!isWatched) Purple500 else Color.White,
            borderColor = if (!isWatched) Purple500 else Color.White,
            enabled = !collectionState.isLoading,
            loading = collectionState.isWatchedLoading,
            modifier = Modifier.fillMaxWidth(),
        )

        Box(modifier = Modifier.align(Alignment.CenterEnd)) {
            DateSelectionMenu(
                expanded = menuVisible.value,
                onDismissRequest = { menuVisible.value = false },
                onSelect = onHistoryClick,
            )
        }
    }
}

@Composable
private fun WatchButton(
    streamingState: StreamingsState,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current

    val plex = streamingState.plex
    val service = streamingState.service
    val loading = streamingState.loading
    val directLink = service?.linkDirect

    WatchNowButton(
        text = when {
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
            plex -> "Plex"
            directLink != null -> service.name
            else -> ""
        },
        logo = when {
            plex -> DEFAULT_PLEX_LOGO_URL
            directLink != null -> service.logo
            else -> null
        },
        enabled = !loading && !streamingState.noServices,
        loading = loading,
        containerColor = when {
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
    streamingState: StreamingsState,
    collectionState: CollectionState,
    isWatched: Boolean,
    watchAgainEnabled: Boolean,
    onHistoryClick: (DateSelectionResult) -> Unit,
    onRemoveHistoryClick: () -> Unit,
    onWhereToWatchClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val menuVisible = remember { mutableStateOf(false) }
    val dateMenuVisible = remember { mutableStateOf(false) }

    val items = buildList {
        add(
            MoreMenuItem(
                text = stringResource(R.string.button_text_where_to_watch),
                icon = painterResource(R.drawable.ic_play),
                onClick = onWhereToWatchClick,
            ),
        )
        if (isWatched) {
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
        ShowActionButtons(
            streamingState = StreamingsState(plex = true),
            collectionState = CollectionState(
                isWatched = true,
                isWatchlist = true,
                episodesPlays = 10,
                episodesAiredCount = 10,
            ),
            watchAgainEnabled = true,
            trailerUrl = "https://youtube.com/watch?v=4mdAbk4dXXY",
            onHistoryClick = {},
            onRemoveHistoryClick = {},
            onWatchlistClick = {},
            onTrailerClick = {},
            onStreamingLongClick = {},
        )
    }
}

@Preview
@Composable
private fun Preview2(
    @PreviewParameter(PreviewParameters::class) collectionState: CollectionState,
) {
    TraktTheme {
        ShowActionButtons(
            streamingState = StreamingsState(plex = true),
            collectionState = collectionState,
            watchAgainEnabled = true,
            trailerUrl = null,
            onHistoryClick = {},
            onRemoveHistoryClick = {},
            onWatchlistClick = {},
            onTrailerClick = {},
            onStreamingLongClick = {},
        )
    }
}

private class PreviewParameters : PreviewParameterProvider<CollectionState> {
    override val values = sequenceOf(
        CollectionState(),
        CollectionState(
            isWatchlistLoading = true,
            isWatchedLoading = true,
        ),
        CollectionState(
            isWatched = true,
            isWatchlist = true,
            episodesPlays = 3,
            episodesAiredCount = 10,
        ),
    )
}
