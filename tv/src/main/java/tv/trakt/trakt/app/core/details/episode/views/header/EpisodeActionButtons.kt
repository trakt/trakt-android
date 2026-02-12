package tv.trakt.trakt.app.core.details.episode.views.header

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement.Absolute.spacedBy
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MenuDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import tv.trakt.trakt.app.Config.DEFAULT_PLEX_LOGO_URL
import tv.trakt.trakt.app.common.ui.buttons.IconButton
import tv.trakt.trakt.app.common.ui.buttons.PrimaryButton
import tv.trakt.trakt.app.common.ui.buttons.WatchNowButton
import tv.trakt.trakt.app.core.details.episode.EpisodeDetailsState
import tv.trakt.trakt.app.core.details.episode.EpisodeDetailsState.StreamingsState
import tv.trakt.trakt.app.core.player.plex.TvPlexPlayerActivity
import tv.trakt.trakt.app.ui.theme.TraktTheme
import tv.trakt.trakt.common.helpers.extensions.openPlexLink
import tv.trakt.trakt.common.helpers.extensions.openWatchNowLink
import tv.trakt.trakt.common.ui.theme.colors.Purple50
import tv.trakt.trakt.common.ui.theme.colors.Purple500
import tv.trakt.trakt.resources.R

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
internal fun EpisodeActionButtons(
    detailsState: EpisodeDetailsState,
    onHistoryClick: () -> Unit,
    onStreamingLongClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val uriHandler = LocalUriHandler.current
    val buttonsWidth = TraktTheme.size.detailsPosterSize * 0.666F

    val seString = stringResource(
        R.string.text_season_episode_number,
        detailsState.episodeDetails?.season ?: 0,
        detailsState.episodeDetails?.number ?: 0,
    )

    val streamingState = detailsState.episodeStreamings

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
                        if (!streamingState.plexStreamUrl.isNullOrBlank()) {
                            val intent = TvPlexPlayerActivity.createIntent(
                                context = context,
                                videoUrl = streamingState.plexStreamUrl,
                                videoTitle = detailsState.showDetails?.title ?: "",
                                videoSubtitle = seString,
                            )
                            context.startActivity(intent)
                        } else {
                            openPlexLink(
                                uriHandler = uriHandler,
                                slug = streamingState.slug?.value,
                                type = "episode",
                                episode = detailsState.episodeDetails?.seasonEpisode,
                            )
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
                !streamingState.plexStreamUrl.isNullOrBlank() ||
                !streamingState.service?.linkDirect.isNullOrBlank()
            ) {
                DropDownButton(
                    streamingState = streamingState,
                    onStreamOnPlexClick = {
                        openPlexLink(
                            uriHandler = uriHandler,
                            slug = streamingState.slug?.value,
                            type = "episode",
                            episode = detailsState.episodeDetails?.seasonEpisode,
                        )
                    },
                    onWhereToWatchClick = onStreamingLongClick,
                )
            }
        }

        val isWatched = remember(detailsState.episodeHistory.episodes?.size) {
            detailsState.episodeHistory.episodesPlays > 0
        }

        PrimaryButton(
            text = stringResource(
                if (isWatched) R.string.button_text_watch_again else R.string.button_text_mark_as_watched,
            ),
            icon = painterResource(if (isWatched) R.drawable.ic_check_double else R.drawable.ic_check),
            onClick = onHistoryClick,
            containerColor = if (!isWatched) Purple50 else Purple500,
            contentColor = if (!isWatched) Purple500 else Color.White,
            borderColor = if (!isWatched) Purple500 else Color.White,
            enabled = !detailsState.isLoading,
            loading = detailsState.isLoading,
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
    val plexStream = !streamingState.plexStreamUrl.isNullOrBlank()
    val service = streamingState.service
    val loading = streamingState.loading
    val directLink = service?.linkDirect

    WatchNowButton(
        text = when {
            plex && plexStream -> stringResource(R.string.button_text_play_now)
            loading || !directLink.isNullOrBlank() || plex -> stringResource(R.string.button_text_stream_on)
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
private fun DropDownButton(
    streamingState: StreamingsState,
    onStreamOnPlexClick: () -> Unit,
    onWhereToWatchClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val plex = streamingState.plex
    val plexStream = !streamingState.plexStreamUrl.isNullOrBlank()

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
            modifier = Modifier.height(42.dp),
        )

        DropdownMenu(
            containerColor = TraktTheme.colors.dialogContainer,
            shape = RoundedCornerShape(20.dp),
            expanded = menuVisible.value,
            onDismissRequest = { menuVisible.value = false },
        ) {
            var focusedIndex by remember { mutableIntStateOf(0) }

            DropdownMenuItem(
                text = {
                    Text(
                        text = "${stringResource(R.string.button_text_stream_on)} Plex".uppercase(),
                        textAlign = TextAlign.Start,
                        style = TraktTheme.typography.buttonPrimary,
                        color = when {
                            focusedIndex == 0 -> TraktTheme.colors.textPrimary
                            else -> TraktTheme.colors.textSecondary
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp)
                            .border(
                                width = 3.dp,
                                color = when {
                                    focusedIndex == 0 -> Color.White
                                    else -> Color.Transparent
                                },
                                shape = RoundedCornerShape(12.dp),
                            )
                            .padding(horizontal = 20.dp, vertical = 16.dp),
                    )
                },
                colors = MenuDefaults.selectableItemColors(
                    containerColor = Color.Transparent,
                ),
                contentPadding = PaddingValues.Zero,
                onClick = {
                    menuVisible.value = false
                    onStreamOnPlexClick()
                },
                modifier = Modifier
                    .onFocusChanged {
                        if (it.isFocused) {
                            focusedIndex = 0
                        }
                    },
            )

            DropdownMenuItem(
                text = {
                    Text(
                        text = stringResource(R.string.button_text_where_to_watch).uppercase(),
                        textAlign = TextAlign.Start,
                        style = TraktTheme.typography.buttonPrimary,
                        color = when {
                            focusedIndex == 1 -> TraktTheme.colors.textPrimary
                            else -> TraktTheme.colors.textSecondary
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp)
                            .border(
                                width = 3.dp,
                                color = when {
                                    focusedIndex == 1 -> Color.White
                                    else -> Color.Transparent
                                },
                                shape = RoundedCornerShape(12.dp),
                            )
                            .padding(horizontal = 20.dp, vertical = 16.dp),
                    )
                },
                colors = MenuDefaults.selectableItemColors(
                    containerColor = Color.Transparent,
                ),
                contentPadding = PaddingValues.Zero,
                onClick = {
                    menuVisible.value = false
                    onWhereToWatchClick()
                },
                modifier = Modifier
                    .onFocusChanged {
                        if (it.isFocused) {
                            focusedIndex = 1
                        }
                    },
            )
        }
    }
}
