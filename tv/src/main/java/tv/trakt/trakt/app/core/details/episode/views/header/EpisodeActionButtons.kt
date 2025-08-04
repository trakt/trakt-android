package tv.trakt.trakt.app.core.details.episode.views.header

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
import tv.trakt.trakt.app.R
import tv.trakt.trakt.app.core.details.episode.EpisodeDetailsState
import tv.trakt.trakt.app.ui.theme.TraktTheme
import tv.trakt.trakt.common.ui.theme.colors.Purple50
import tv.trakt.trakt.common.ui.theme.colors.Purple500

@Composable
internal fun EpisodeActionButtons(
    streamingState: EpisodeDetailsState.StreamingsState,
    historyState: EpisodeDetailsState.HistoryState,
    onHistoryClick: () -> Unit,
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
            text = if (streamingState.isLoading || directLink != null) {
                stringResource(R.string.stream_on)
            } else {
                stringResource(R.string.stream_unavailable)
            },
            name = if (directLink != null) service.name else "",
            logo = if (directLink != null) service.logo else null,
            enabled = !streamingState.isLoading && directLink != null,
            loading = streamingState.isLoading,
            containerColor = service?.color ?: TraktTheme.colors.primaryButtonContainerDisabled,
            onClick = {
                if (directLink == null) {
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

        val isWatched = remember(historyState.episodes?.size) { historyState.episodesPlays > 0 }
        PrimaryButton(
            text = stringResource(if (isWatched) R.string.add_watched_again else R.string.add_watched),
            icon = painterResource(if (isWatched) R.drawable.ic_check_double else R.drawable.ic_check),
            onClick = onHistoryClick,
            containerColor = if (!isWatched) Purple50 else Purple500,
            contentColor = if (!isWatched) Purple500 else Color.White,
            borderColor = if (!isWatched) Purple500 else Color.White,
            enabled = !historyState.isLoading,
            loading = historyState.isLoading,
        )
    }
}
