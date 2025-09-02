package tv.trakt.trakt.app.core.details.episode.views.header

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
import tv.trakt.trakt.app.Config.DEFAULT_PLEX_LOGO_URL
import tv.trakt.trakt.app.common.ui.buttons.PrimaryButton
import tv.trakt.trakt.app.common.ui.buttons.WatchNowButton
import tv.trakt.trakt.app.core.details.episode.EpisodeDetailsState
import tv.trakt.trakt.app.core.details.episode.EpisodeDetailsState.StreamingsState
import tv.trakt.trakt.app.helpers.extensions.openPlexLink
import tv.trakt.trakt.app.helpers.extensions.openWatchNowLink
import tv.trakt.trakt.app.ui.theme.TraktTheme
import tv.trakt.trakt.common.model.SeasonEpisode
import tv.trakt.trakt.common.ui.theme.colors.Purple50
import tv.trakt.trakt.common.ui.theme.colors.Purple500
import tv.trakt.trakt.resources.R

@Composable
internal fun EpisodeActionButtons(
    streamingState: StreamingsState,
    historyState: EpisodeDetailsState.HistoryState,
    episode: SeasonEpisode?,
    onHistoryClick: () -> Unit,
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
                        type = "episode",
                        episode = episode,
                    )
                } else {
                    openWatchNowLink(
                        context = context,
                        uriHandler = uriHandler,
                        link = streamingState.service?.linkDirect,
                    )
                }
            },
        )

        val isWatched = remember(historyState.episodes?.size) { historyState.episodesPlays > 0 }
        PrimaryButton(
            text = stringResource(
                if (isWatched) R.string.button_text_watch_again else R.string.button_text_mark_as_watched,
            ),
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

@Composable
private fun WatchButton(
    streamingState: StreamingsState,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
) {
    val context = LocalContext.current

    val plex = streamingState.plex
    val service = streamingState.service
    val loading = streamingState.loading
    val directLink = service?.linkDirect

    WatchNowButton(
        text = when {
            loading || !directLink.isNullOrBlank() || plex -> stringResource(R.string.button_text_stream_on)
            streamingState.noServices -> stringResource(R.string.button_text_no_services)
            else -> stringResource(R.string.button_text_where_to_watch)
        },
        secondaryText = when {
            !loading && (plex || directLink != null) && streamingState.info != null -> {
                streamingState.info.get(context)
            }
            else -> null
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
            service?.color != null -> service.color
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
    )
}
