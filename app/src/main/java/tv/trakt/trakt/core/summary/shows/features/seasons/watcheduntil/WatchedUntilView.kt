package tv.trakt.trakt.core.summary.shows.features.seasons.watcheduntil

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.collections.immutable.persistentListOf
import tv.trakt.trakt.common.helpers.LoadingState
import tv.trakt.trakt.common.helpers.preview.PreviewData
import tv.trakt.trakt.common.ui.composables.FilmProgressIndicator
import tv.trakt.trakt.core.summary.shows.features.seasons.watcheduntil.WatchedUntilAction.Now
import tv.trakt.trakt.core.summary.shows.features.seasons.watcheduntil.WatchedUntilAction.OtherDate
import tv.trakt.trakt.core.summary.shows.features.seasons.watcheduntil.WatchedUntilAction.ReleaseDate
import tv.trakt.trakt.core.summary.shows.features.seasons.watcheduntil.ui.ActionButtons
import tv.trakt.trakt.core.summary.shows.features.seasons.watcheduntil.ui.OtherDateBoundButtons
import tv.trakt.trakt.core.summary.shows.features.seasons.watcheduntil.ui.SegmentGap
import tv.trakt.trakt.core.summary.shows.features.seasons.watcheduntil.ui.WatchedTimestampsList
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.components.TraktHeader
import tv.trakt.trakt.ui.components.buttons.PrimaryButton
import tv.trakt.trakt.ui.components.dateselection.TraktDatePicker
import tv.trakt.trakt.ui.components.dateselection.TraktTimePicker
import tv.trakt.trakt.ui.theme.TraktTheme
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZoneOffset.UTC

internal enum class WatchedUntilAction {
    Now,
    ReleaseDate,
    OtherDate,
}

internal enum class OtherDateBound {
    Start,
    End,
}

@Composable
internal fun WatchedUntilView(
    viewModel: WatchedUntilViewModel,
    onDismiss: () -> Unit = {},
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(state.success) {
        if (state.success == true) {
            onDismiss()
        }
    }

    WatchedUntilContent(
        state = state,
        onCancel = onDismiss,
        onConfirm = viewModel::addToWatched,
    )
}

@Composable
private fun WatchedUntilContent(
    state: WatchedUntilState,
    onCancel: () -> Unit = {},
    onConfirm: (
        action: WatchedUntilAction,
        otherBound: OtherDateBound?,
        otherAnchor: Instant?,
    ) -> Unit = { _, _, _ -> },
) {
    var selectedAction by remember { mutableStateOf(Now) }
    var otherBound by remember { mutableStateOf<OtherDateBound?>(null) }
    var otherAnchor by remember { mutableStateOf<Instant?>(null) }

    // Bound the date picker is being opened for, and the date awaiting a time.
    var pendingBound by remember { mutableStateOf<OtherDateBound?>(null) }
    var pendingDate by remember { mutableStateOf<Instant?>(null) }

    fun resetOtherDate() {
        otherBound = null
        otherAnchor = null
        pendingBound = null
        pendingDate = null
    }

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

        Column(
            verticalArrangement = spacedBy(0.dp),
            modifier = Modifier
                .fillMaxWidth()
                .animateContentSize(),
        ) {
            ActionButtons(
                enabled = !state.loading.isLoading && !state.loadingWatched.isLoading,
                selected = selectedAction,
                onNowClick = {
                    selectedAction = Now
                    resetOtherDate()
                },
                onReleaseClick = {
                    selectedAction = ReleaseDate
                    resetOtherDate()
                },
                onOtherClick = { selectedAction = OtherDate },
            )

            if (selectedAction == OtherDate) {
                OtherDateBoundButtons(
                    selected = otherBound,
                    anchor = otherAnchor,
                    enabled = !state.loading.isLoading && !state.loadingWatched.isLoading,
                    onBoundClick = { bound ->
                        pendingBound = bound
                        pendingDate = null
                    },
                    modifier = Modifier
                        .padding(top = SegmentGap),
                )
            }
        }

        if (!state.episodes.isNullOrEmpty()) {
            WatchedTimestampsList(
                episodes = state.episodes,
                selectedAction = selectedAction,
                otherBound = otherBound,
                otherAnchor = otherAnchor,
                modifier = Modifier.padding(top = 32.dp),
            )
        } else if (state.episodes.isNullOrEmpty() && state.loading.isLoading) {
            FilmProgressIndicator(
                size = 32.dp,
                color = TraktTheme.colors.textPrimary,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 32.dp),
            )
        }

        Column(
            verticalArrangement = spacedBy(6.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 32.dp),
        ) {
            val awaitingOtherDate = selectedAction == OtherDate && otherAnchor == null
            PrimaryButton(
                text = stringResource(R.string.button_text_mark_as_watched),
                enabled = !state.loading.isLoading &&
                    !state.loadingWatched.isLoading &&
                    !awaitingOtherDate,
                loading = state.loadingWatched.isLoading && !awaitingOtherDate,
                onClick = { onConfirm(selectedAction, otherBound, otherAnchor) },
                modifier = Modifier.fillMaxWidth(),
            )

            PrimaryButton(
                text = stringResource(R.string.button_text_cancel),
                enabled = !state.loading.isLoading && !state.loadingWatched.isLoading,
                onClick = onCancel,
                containerColor = TraktTheme.colors.primaryButtonContainerDisabled,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }

    TraktDatePicker(
        active = pendingBound != null && pendingDate == null,
        onDateSelected = { pendingDate = it },
        onDismiss = {
            pendingBound = null
            pendingDate = null
        },
    )

    TraktTimePicker(
        active = pendingDate != null,
        selectedDate = pendingDate,
        onDateTimeSelected = { dateTimeUtc ->
            val localDateTime = LocalDateTime.ofInstant(dateTimeUtc, UTC)
            otherAnchor = localDateTime.atZone(ZoneId.systemDefault()).toInstant()
            otherBound = pendingBound
            pendingBound = null
            pendingDate = null
        },
        onDismiss = { pendingDate = null },
    )
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
