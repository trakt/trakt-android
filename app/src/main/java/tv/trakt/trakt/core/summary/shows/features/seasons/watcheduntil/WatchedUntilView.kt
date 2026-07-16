package tv.trakt.trakt.core.summary.shows.features.seasons.watcheduntil

import androidx.compose.foundation.layout.Arrangement.SpaceBetween
import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight.Companion.W400
import androidx.compose.ui.text.font.FontWeight.Companion.W600
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import tv.trakt.trakt.common.helpers.LoadingState
import tv.trakt.trakt.common.helpers.extensions.longDateFormat
import tv.trakt.trakt.common.helpers.extensions.nowUtcInstant
import tv.trakt.trakt.common.helpers.extensions.timeFormat
import tv.trakt.trakt.common.helpers.extensions.toLocal
import tv.trakt.trakt.common.helpers.preview.PreviewData
import tv.trakt.trakt.common.model.Episode
import tv.trakt.trakt.common.ui.composables.FilmProgressIndicator
import tv.trakt.trakt.core.summary.shows.features.seasons.watcheduntil.WatchedUntilAction.Now
import tv.trakt.trakt.core.summary.shows.features.seasons.watcheduntil.WatchedUntilAction.OtherDate
import tv.trakt.trakt.core.summary.shows.features.seasons.watcheduntil.WatchedUntilAction.ReleaseDate
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.components.TraktHeader
import tv.trakt.trakt.ui.components.buttons.GhostButton
import tv.trakt.trakt.ui.components.buttons.PrimaryButton
import tv.trakt.trakt.ui.theme.TraktTheme
import java.time.Instant

private enum class WatchedUntilAction {
    Now,
    ReleaseDate,
    OtherDate,
}

private val TimestampsMaxHeight = 300.dp

@Composable
internal fun WatchedUntilView(
    viewModel: WatchedUntilViewModel,
    onDismiss: () -> Unit = {},
    onMarkAsWatched: () -> Unit = {},
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    WatchedUntilContent(
        state = state,
        onCancel = onDismiss,
        onMarkAsWatched = onMarkAsWatched,
    )
}

@Composable
private fun WatchedUntilContent(
    state: WatchedUntilState,
    onCancel: () -> Unit = {},
    onMarkAsWatched: () -> Unit = {},
) {
    var selectedAction by remember { mutableStateOf(Now) }

    Column(
        verticalArrangement = spacedBy(0.dp),
        modifier = Modifier
            .padding(horizontal = 24.dp)
            .padding(bottom = 24.dp),
    ) {
        TraktHeader(
            title = state.show?.title.orEmpty(),
            subtitle = when {
                state.episodes.isNullOrEmpty() -> ""
                else -> stringResource(R.string.text_episodes_watched, state.episodes.size)
            },
            modifier = Modifier.padding(bottom = 16.dp),
        )

        ActionButtons(
            enabled = !state.loading.isLoading,
            selected = selectedAction,
            onNowClick = { selectedAction = Now },
            onReleaseClick = { selectedAction = ReleaseDate },
            onOtherClick = { selectedAction = OtherDate },
        )

        if (!state.episodes.isNullOrEmpty()) {
            WatchedTimestampsList(
                episodes = state.episodes,
                selectedAction = selectedAction,
                modifier = Modifier.padding(top = 20.dp),
            )
        } else if (state.episodes.isNullOrEmpty() && state.loading.isLoading) {
            FilmProgressIndicator(
                size = 32.dp,
                color = TraktTheme.colors.textPrimary,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 24.dp)
                    .padding(bottom = 8.dp),
            )
        }

        Column(
            verticalArrangement = spacedBy(8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 32.dp),
        ) {
            PrimaryButton(
                text = stringResource(R.string.button_text_mark_as_watched),
                enabled = !state.loading.isLoading,
                onClick = onMarkAsWatched,
                modifier = Modifier.fillMaxWidth(),
            )

            PrimaryButton(
                text = stringResource(R.string.button_text_cancel),
                enabled = !state.loading.isLoading,
                onClick = onCancel,
                containerColor = TraktTheme.colors.primaryButtonContainerDisabled,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Composable
private fun WatchedTimestampsList(
    episodes: ImmutableList<Episode>,
    selectedAction: WatchedUntilAction,
    modifier: Modifier = Modifier,
) {
    val timestamps = remember(episodes, selectedAction) {
        when (selectedAction) {
            Now -> {
                // Selected (last) episode is anchored to now; each earlier one
                // is the next timestamp minus the runtime of the episode just
                // watched (the later one).
                var accumulated = nowUtcInstant()
                val result = arrayOfNulls<Instant>(episodes.size)
                for (index in episodes.indices.reversed()) {
                    result[index] = accumulated
                    accumulated = accumulated.minusMillis(
                        episodes[index].runtime?.inWholeMilliseconds ?: 0,
                    )
                }
                result.toList()
            }

            ReleaseDate, OtherDate -> {
                episodes.map { it.releasedAt }
            }
        }
    }

    Column(
        verticalArrangement = spacedBy(8.dp),
        modifier = modifier,
    ) {
        Text(
            text = stringResource(R.string.text_watch_until_here_preview_title),
            style = TraktTheme.typography.meta.copy(fontWeight = W400),
            color = TraktTheme.colors.textSecondary,
            modifier = Modifier.padding(bottom = 4.dp),
        )

        Column(
            verticalArrangement = spacedBy(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = TimestampsMaxHeight)
                .verticalScroll(rememberScrollState()),
        ) {
            episodes.forEachIndexed { index, episode ->
                EpisodeRow(
                    episode = episode,
                    timestamp = timestamps.getOrNull(index),
                )
            }
        }
    }
}

@Composable
private fun EpisodeRow(
    episode: Episode,
    timestamp: Instant?,
    modifier: Modifier = Modifier,
) {
    val dateFormat = longDateFormat()
    val timeFormat = timeFormat()

    val timestampText = timestamp?.toLocal()?.let {
        "${it.format(dateFormat)}  •  ${it.format(timeFormat)}"
    }.orEmpty()

    Row(
        horizontalArrangement = SpaceBetween,
        verticalAlignment = CenterVertically,
        modifier = modifier.fillMaxWidth(),
    ) {
        Text(
            text = stringResource(
                R.string.episode_footer_season_episode,
                episode.season,
                episode.number,
            ),
            style = TraktTheme.typography.buttonPrimary.copy(
                fontWeight = W600,
            ),
            color = TraktTheme.colors.textPrimary,
        )

        Text(
            text = timestampText,
            style = TraktTheme.typography.meta,
            color = TraktTheme.colors.textSecondary,
        )
    }
}

@Composable
private fun ActionButtons(
    selected: WatchedUntilAction,
    enabled: Boolean,
    modifier: Modifier = Modifier,
    onNowClick: () -> Unit = {},
    onReleaseClick: () -> Unit = {},
    onOtherClick: () -> Unit = {},
) {
    Column(
        verticalArrangement = spacedBy(TraktTheme.spacing.contextItemsSpace / 2),
        modifier = modifier
            .graphicsLayer {
                translationX = -8.dp.toPx()
            },
    ) {
        GhostButton(
            enabled = enabled,
            text = stringResource(R.string.button_text_mark_as_watched_now),
            icon = painterResource(R.drawable.ic_check),
            iconSize = 21.dp,
            iconSpace = 16.dp,
            onClick = onNowClick,
            modifier = Modifier
                .alpha(if (selected == Now) 1f else 0.25f),
        )
        GhostButton(
            enabled = enabled,
            text = stringResource(R.string.button_text_mark_as_watched_release_date),
            icon = painterResource(R.drawable.ic_calendar_time_trakt),
            iconSize = 21.dp,
            iconSpace = 17.dp,
            onClick = onReleaseClick,
            modifier = Modifier
                .alpha(if (selected == ReleaseDate) 1f else 0.25f),
        )
        GhostButton(
            enabled = enabled,
            text = stringResource(R.string.button_text_mark_as_watched_other_date),
            icon = painterResource(R.drawable.ic_edit),
            iconSize = 22.dp,
            iconSpace = 16.dp,
            onClick = onOtherClick,
            modifier = Modifier
                .alpha(if (selected == OtherDate) 1f else 0.25f),
        )
    }
}

@Preview(
    device = "id:pixel_5",
    showBackground = true,
    backgroundColor = 0xFF212427,
)
@Composable
private fun Preview() {
    TraktTheme {
        WatchedUntilContent(
            state = WatchedUntilState(
                show = PreviewData.show1,
                episodes = persistentListOf(
                    PreviewData.episode1,
                    PreviewData.episode1.copy(number = 4),
                    PreviewData.episode1.copy(number = 5),
                ),
            ),
        )
    }
}

@Preview(
    device = "id:pixel_5",
    showBackground = true,
    backgroundColor = 0xFF212427,
)
@Composable
private fun Preview2() {
    TraktTheme {
        WatchedUntilContent(
            state = WatchedUntilState(
                loading = LoadingState.Loading,
                show = PreviewData.show1,
                episodes = persistentListOf(),
            ),
        )
    }
}
