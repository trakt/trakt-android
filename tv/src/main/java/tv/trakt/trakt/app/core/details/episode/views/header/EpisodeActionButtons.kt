package tv.trakt.trakt.app.core.details.episode.views.header

import androidx.compose.foundation.layout.Arrangement.Absolute.spacedBy
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.ui.Modifier
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
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import tv.trakt.trakt.app.Config.DEFAULT_PLEX_LOGO_URL
import tv.trakt.trakt.app.common.model.SyncHistoryEpisodeItem
import tv.trakt.trakt.app.common.ui.buttons.OutlineButton
import tv.trakt.trakt.app.common.ui.buttons.PrimaryButton
import tv.trakt.trakt.app.common.ui.buttons.WatchNowButton
import tv.trakt.trakt.app.common.ui.menus.TvDropdownMenu
import tv.trakt.trakt.app.common.ui.menus.TvDropdownMenuItem
import tv.trakt.trakt.app.core.details.episode.EpisodeDetailsState
import tv.trakt.trakt.app.core.details.episode.EpisodeDetailsState.HistoryState
import tv.trakt.trakt.app.core.details.episode.EpisodeDetailsState.StreamingsState
import tv.trakt.trakt.app.core.details.episode.usecases.streamings.GetPlexUseCase
import tv.trakt.trakt.app.core.details.ui.dateselection.DateSelectionMenu
import tv.trakt.trakt.app.core.player.plex.TvPlexPlayerActivity
import tv.trakt.trakt.app.ui.theme.TraktTheme
import tv.trakt.trakt.common.helpers.extensions.openPlexLink
import tv.trakt.trakt.common.helpers.extensions.openWatchNowLink
import tv.trakt.trakt.common.model.DateSelectionResult
import tv.trakt.trakt.common.model.Episode
import tv.trakt.trakt.common.model.Ids
import tv.trakt.trakt.common.model.MediaType
import tv.trakt.trakt.common.model.Rating
import tv.trakt.trakt.common.model.Show
import tv.trakt.trakt.common.model.SlugId
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.ui.theme.colors.Purple500
import tv.trakt.trakt.resources.R
import java.time.ZonedDateTime

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
internal fun EpisodeActionButtons(
    detailsState: EpisodeDetailsState,
    onHistoryClick: (DateSelectionResult) -> Unit,
    onRemoveHistoryClick: () -> Unit,
    onStreamingLongClick: () -> Unit,
    onDropClick: () -> Unit,
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
    val historyState = detailsState.episodeHistory
    val isWatched = remember(historyState.episodes?.size) {
        historyState.episodesPlays > 0
    }

    Column(
        verticalArrangement = spacedBy(8.dp),
        modifier = modifier.width(buttonsWidth),
    ) {
        WatchButton(
            streamingState = streamingState,
            onLongClick = onStreamingLongClick,
            onClick = {
                if (streamingState.plex) {
                    if (!streamingState.plexStream?.primaryUrl.isNullOrBlank()) {
                        val intent = TvPlexPlayerActivity.createIntent(
                            context = context,
                            mediaId = detailsState.episodeDetails?.ids?.trakt ?: return@WatchButton,
                            mediaType = MediaType.Episode,
                            primaryVideoUrl = streamingState.plexStream.primaryUrl,
                            secondaryVideoUrls = streamingState.plexStream.secondaryUrls,
                            videoTitle = detailsState.showDetails?.title ?: "",
                            videoSubtitle = seString,
                            videoProgress = streamingState.plexStream.progress,
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
            modifier = Modifier.fillMaxWidth(),
        )

        MarkAsWatchedButton(
            isHistory = isWatched,
            historyState = historyState,
            onHistoryClick = onHistoryClick,
            onRemoveHistoryClick = onRemoveHistoryClick,
            modifier = Modifier.fillMaxWidth(),
        )

        MoreButton(
            streamingState = streamingState,
            historyState = historyState,
            isWatched = isWatched,
            watchAgainEnabled = detailsState.user?.settings?.watchOnlyOnce != true,
            onHistoryClick = onHistoryClick,
            onRemoveHistoryClick = onRemoveHistoryClick,
            onStreamOnPlexClick = {
                openPlexLink(
                    uriHandler = uriHandler,
                    slug = streamingState.slug?.value,
                    type = "episode",
                    episode = detailsState.episodeDetails?.seasonEpisode,
                )
            },
            onWhereToWatchClick = onStreamingLongClick,
            onDropClick = onDropClick,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
private fun MarkAsWatchedButton(
    isHistory: Boolean,
    historyState: HistoryState,
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
            enabled = !historyState.isLoading,
            loading = historyState.isLoading,
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
    streamingState: StreamingsState,
    historyState: HistoryState,
    isWatched: Boolean,
    watchAgainEnabled: Boolean,
    onHistoryClick: (DateSelectionResult) -> Unit,
    onRemoveHistoryClick: () -> Unit,
    onStreamOnPlexClick: () -> Unit,
    onWhereToWatchClick: () -> Unit,
    onDropClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val plexStream = streamingState.plex && !streamingState.plexStream?.primaryUrl.isNullOrBlank()
    val plexProgress = streamingState.plexStream?.progress ?: 0F

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
                    text = stringResource(R.string.button_text_drop_episode).uppercase(),
                    onClick = onDropClick,
                ),
            )
        }
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
            enabled = !historyState.isLoading && !streamingState.loading,
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
        EpisodeActionButtons(
            detailsState = EpisodeDetailsState(
                episodeHistory = HistoryState(
                    episodes = persistentListOf(previewHistoryItem()),
                ),
                episodeStreamings = StreamingsState(
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
            onStreamingLongClick = {},
            onDropClick = {},
        )
    }
}

@Preview
@Composable
private fun Preview2(
    @PreviewParameter(PreviewParameters::class) historyState: HistoryState,
) {
    TraktTheme {
        EpisodeActionButtons(
            detailsState = EpisodeDetailsState(
                episodeHistory = historyState,
                episodeStreamings = StreamingsState(plex = true),
            ),
            onHistoryClick = {},
            onRemoveHistoryClick = {},
            onStreamingLongClick = {},
            onDropClick = {},
        )
    }
}

private class PreviewParameters : PreviewParameterProvider<HistoryState> {
    override val values = sequenceOf(
        HistoryState(),
        HistoryState(isLoading = true),
        HistoryState(episodes = persistentListOf(previewHistoryItem())),
    )
}

private fun previewHistoryItem(): SyncHistoryEpisodeItem {
    val ids = Ids(trakt = TraktId(1), slug = SlugId("preview"))
    val rating = Rating(rating = 8F, votes = 100)
    return SyncHistoryEpisodeItem(
        id = 1,
        watchedAt = ZonedDateTime.now(),
        episode = Episode(
            ids = ids,
            type = null,
            number = 1,
            season = 1,
            title = "Pilot",
            numberAbs = null,
            overview = null,
            rating = rating,
            commentCount = 0,
            runtime = null,
            originalTitle = "Pilot",
            images = null,
            updatedAt = null,
            firstAired = null,
            effectiveReleaseDate = null,
        ),
        show = Show(
            ids = ids,
            title = "Preview",
            titleOriginal = null,
            overview = null,
            network = null,
            status = null,
            year = null,
            genres = persistentListOf(),
            images = null,
            colors = null,
            rating = rating,
            certification = null,
            trailer = null,
            runtime = null,
            totalRuntime = null,
            airedEpisodes = 0,
            country = null,
            languages = persistentListOf(),
            releasedAt = null,
        ),
    )
}
