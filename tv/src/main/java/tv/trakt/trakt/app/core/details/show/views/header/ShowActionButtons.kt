package tv.trakt.trakt.app.core.details.show.views.header

import androidx.compose.foundation.layout.Arrangement.Absolute.spacedBy
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import tv.trakt.trakt.app.R
import tv.trakt.trakt.app.common.ui.buttons.PrimaryButton
import tv.trakt.trakt.app.common.ui.buttons.WatchNowButton
import tv.trakt.trakt.app.core.details.show.ShowDetailsState.CollectionState
import tv.trakt.trakt.app.core.details.show.ShowDetailsState.StreamingsState
import tv.trakt.trakt.app.helpers.extensions.openWatchNowLink
import tv.trakt.trakt.app.ui.theme.TraktTheme
import tv.trakt.trakt.common.ui.theme.colors.Blue50
import tv.trakt.trakt.common.ui.theme.colors.Blue500
import tv.trakt.trakt.common.ui.theme.colors.Purple50
import tv.trakt.trakt.common.ui.theme.colors.Purple500

@Composable
internal fun ShowActionButtons(
    streamingState: StreamingsState,
    collectionState: CollectionState,
    onHistoryClick: () -> Unit,
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
        val service = streamingState.service
        val loading = streamingState.loading
        val directLink = service?.linkDirect

        WatchNowButton(
            text = when {
                loading || !directLink.isNullOrBlank() -> stringResource(R.string.stream_on)
                streamingState.noServices -> stringResource(R.string.stream_no_services)
                else -> stringResource(R.string.stream_more_options)
            },
            secondaryText = when {
                !loading && directLink != null && streamingState.info != null -> {
                    streamingState.info.get(context)
                }
                else -> null
            },
            name = if (directLink != null) service.name else "",
            logo = if (directLink != null) service.logo else null,
            enabled = !loading && !streamingState.noServices,
            loading = loading,
            containerColor = service?.color ?: TraktTheme.colors.primaryButtonContainerDisabled,
            onLongClick = onStreamingLongClick,
            onClick = {
                if (directLink == null) {
                    onStreamingLongClick()
                    return@WatchNowButton
                }
                openWatchNowLink(
                    context = context,
                    uriHandler = uriHandler,
                    link = directLink,
                )
            },
        )

        val isWatched = remember(collectionState.isWatched) { collectionState.isWatched }
        val isAllWatched = remember(collectionState.isAllWatched) { collectionState.isAllWatched }
        PrimaryButton(
            text = stringResource(if (isAllWatched) R.string.add_watched_again else R.string.add_watched),
            icon = painterResource(if (isAllWatched) R.drawable.ic_check_double else R.drawable.ic_check),
            onClick = onHistoryClick,
            containerColor = if (!isWatched) Purple50 else Purple500,
            contentColor = if (!isWatched) Purple500 else Color.White,
            borderColor = if (!isWatched) Purple500 else Color.White,
            enabled = !collectionState.isLoading,
            loading = collectionState.isWatchedLoading,
        )

        val isWatchlist = remember(collectionState.isWatchlist) { collectionState.isWatchlist }
        PrimaryButton(
            text = stringResource(if (isWatchlist) R.string.in_watchlist else R.string.add_watchlist),
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
