package tv.trakt.trakt.app.core.details.movie.views.header

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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import tv.trakt.trakt.app.common.ui.buttons.PrimaryButton
import tv.trakt.trakt.app.common.ui.buttons.WatchNowButton
import tv.trakt.trakt.app.core.details.movie.MovieDetailsState.CollectionState
import tv.trakt.trakt.app.core.details.movie.MovieDetailsState.StreamingsState
import tv.trakt.trakt.app.helpers.extensions.openWatchNowLink
import tv.trakt.trakt.app.ui.theme.TraktTheme
import tv.trakt.trakt.common.ui.theme.colors.Blue50
import tv.trakt.trakt.common.ui.theme.colors.Blue500
import tv.trakt.trakt.common.ui.theme.colors.Purple50
import tv.trakt.trakt.common.ui.theme.colors.Purple500
import tv.trakt.trakt.resources.R

@Composable
internal fun MovieActionButtons(
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
                loading || !directLink.isNullOrBlank() -> stringResource(R.string.button_text_stream_on)
                streamingState.noServices -> stringResource(R.string.button_text_no_services)
                else -> stringResource(R.string.button_text_where_to_watch)
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

        val isHistory = remember(collectionState.isHistory) { collectionState.isHistory }
        PrimaryButton(
            text = stringResource(
                if (isHistory) R.string.button_text_watch_again else R.string.button_text_mark_as_watched,
            ),
            icon = painterResource(if (isHistory) R.drawable.ic_check_double else R.drawable.ic_check),
            onClick = onHistoryClick,
            containerColor = if (isHistory) Purple500 else Purple50,
            contentColor = if (isHistory) Color.White else Purple500,
            borderColor = if (isHistory) Color.White else Purple500,
            enabled = !collectionState.isLoading,
            loading = collectionState.isHistoryLoading,
        )

        val isWatchlist = remember(collectionState.isWatchlist) { collectionState.isWatchlist }
        PrimaryButton(
            text = stringResource(if (isWatchlist) R.string.button_text_watchlist else R.string.button_text_watchlist),
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

@Preview
@Composable
private fun Preview(
    @PreviewParameter(PreviewParameters::class) collectionState: CollectionState,
) {
    TraktTheme {
        MovieActionButtons(
            streamingState = StreamingsState(
                loading = true,
            ),
            collectionState = collectionState,
            onHistoryClick = {},
            onWatchlistClick = {},
            onStreamingLongClick = {},
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
