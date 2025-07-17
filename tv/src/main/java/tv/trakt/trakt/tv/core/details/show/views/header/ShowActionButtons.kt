package tv.trakt.trakt.tv.core.details.show.views.header

import PrimaryButton
import WatchNowButton
import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
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
import androidx.core.net.toUri
import tv.trakt.trakt.common.ui.theme.colors.Blue50
import tv.trakt.trakt.common.ui.theme.colors.Blue500
import tv.trakt.trakt.common.ui.theme.colors.Purple50
import tv.trakt.trakt.common.ui.theme.colors.Purple500
import tv.trakt.trakt.tv.Config
import tv.trakt.trakt.tv.R
import tv.trakt.trakt.tv.core.details.show.ShowDetailsState.CollectionState
import tv.trakt.trakt.tv.core.details.show.ShowDetailsState.StreamingsState
import tv.trakt.trakt.tv.ui.theme.TraktTheme

@Composable
internal fun ShowActionButtons(
    streamingState: StreamingsState,
    collectionState: CollectionState,
    onHistoryClick: () -> Unit,
    onWatchlistClick: () -> Unit,
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
        val directLink = service?.linkDirect

        WatchNowButton(
            name = if (directLink != null) service.name else "Plex",
            logo = if (directLink != null) service.logo else Config.PLEX_IMAGE_URL,
            enabled = !streamingState.isLoading,
            loading = streamingState.isLoading,
            containerColor = service?.color ?: TraktTheme.colors.primaryButtonContainerDisabled,
            onClick = {
                if (directLink == null) {
                    uriHandler.openUri("${Config.PLEX_BASE_URL}show/${streamingState.slug?.value}")
                    return@WatchNowButton
                }
                if (directLink.contains("netflix", ignoreCase = true)) {
                    val intent = Intent(Intent.ACTION_VIEW)
                    intent.data = directLink.toUri()
                    intent.flags = FLAG_ACTIVITY_NEW_TASK or FLAG_ACTIVITY_CLEAR_TASK
                    intent.putExtra("source", "30")
                    context.startActivity(intent)
                    return@WatchNowButton
                }
                uriHandler.openUri(directLink)
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
