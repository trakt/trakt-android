package tv.trakt.trakt.app.core.details.show.views.header

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
import androidx.compose.ui.unit.dp
import tv.trakt.trakt.app.Config.DEFAULT_PLEX_LOGO_URL
import tv.trakt.trakt.app.common.ui.buttons.IconButton
import tv.trakt.trakt.app.common.ui.buttons.PrimaryButton
import tv.trakt.trakt.app.common.ui.buttons.WatchNowButton
import tv.trakt.trakt.app.common.ui.menus.TvDropdownMenu
import tv.trakt.trakt.app.common.ui.menus.TvDropdownMenuItem
import tv.trakt.trakt.app.core.details.show.ShowDetailsState.CollectionState
import tv.trakt.trakt.app.core.details.show.ShowDetailsState.StreamingsState
import tv.trakt.trakt.app.core.details.ui.dateselection.DateSelectionMenu
import tv.trakt.trakt.app.ui.theme.TraktTheme
import tv.trakt.trakt.common.helpers.extensions.openPlexLink
import tv.trakt.trakt.common.helpers.extensions.openWatchNowLink
import tv.trakt.trakt.common.model.DateSelectionResult
import tv.trakt.trakt.common.ui.theme.colors.Blue50
import tv.trakt.trakt.common.ui.theme.colors.Blue500
import tv.trakt.trakt.common.ui.theme.colors.Purple50
import tv.trakt.trakt.common.ui.theme.colors.Purple500
import tv.trakt.trakt.resources.R

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
internal fun ShowActionButtons(
    streamingState: StreamingsState,
    collectionState: CollectionState,
    watchAgainEnabled: Boolean,
    onHistoryClick: (DateSelectionResult) -> Unit,
    onRemoveHistoryClick: () -> Unit,
    onWatchlistClick: () -> Unit,
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
        Row(
            horizontalArrangement = spacedBy(6.dp),
            verticalAlignment = CenterVertically,
            modifier = Modifier.fillMaxWidth(),
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
                modifier = Modifier.weight(1f, false),
            )

            if (streamingState.plex || !streamingState.service?.linkDirect.isNullOrBlank()) {
                DropDownButton(
                    enabled = !streamingState.loading,
                    onWhereToWatchClick = onStreamingLongClick,
                )
            }
        }

        val isWatched = remember(collectionState.isWatched) { collectionState.isWatched }
        val isAllWatched = remember(collectionState.isAllWatched) { collectionState.isAllWatched }
        val dateMenuVisible = remember { mutableStateOf(false) }

        Row(
            horizontalArrangement = spacedBy(6.dp),
            verticalAlignment = CenterVertically,
            modifier = Modifier.fillMaxWidth(),
        ) {
            MarkAsWatchedButton(
                isWatched = isWatched,
                isAllWatched = isAllWatched,
                collectionState = collectionState,
                onHistoryClick = onHistoryClick,
                onRemoveHistoryClick = onRemoveHistoryClick,
                modifier = Modifier.weight(1f, false),
            )

            if (!collectionState.isLoading && isWatched) {
                Box(
                    contentAlignment = Alignment.Center,
                ) {
                    HistoryDropDownButton(
                        enabled = !collectionState.isLoading,
                        watchAgainEnabled = watchAgainEnabled,
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
            text = stringResource(R.string.button_text_watchlist),
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

    Box(modifier = modifier.fillMaxWidth()) {
        PrimaryButton(
            text = stringResource(
                if (isWatched) R.string.tag_text_watched else R.string.button_text_mark_as_watched,
            ),
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

@Composable
private fun DropDownButton(
    enabled: Boolean,
    onWhereToWatchClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    IconButton(
        icon = painterResource(R.drawable.ic_more_vertical),
        iconSize = 14.dp,
        size = 32.dp,
        onClick = onWhereToWatchClick,
        containerColor = Color.Transparent,
        contentColor = TraktTheme.colors.primaryButtonContent,
        borderColor = Color.White,
        enabled = enabled,
        modifier = modifier.height(42.dp),
    )
}
