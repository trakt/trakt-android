package tv.trakt.trakt.core.summary.shows.features.progress.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import tv.trakt.trakt.common.helpers.extensions.PreviewTraktLightDark
import tv.trakt.trakt.common.helpers.extensions.onClick
import tv.trakt.trakt.common.ui.composables.FilmProgressIndicator
import tv.trakt.trakt.helpers.extensions.TraktThemePreview
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.theme.TraktTheme

internal enum class EpisodeWatchState {
    Watched,
    Unwatched,
    Upcoming,
}

@Composable
internal fun EpisodeProgressChip(
    number: Int,
    state: EpisodeWatchState,
    modifier: Modifier = Modifier,
    upNext: Boolean = false,
    busy: Boolean = false,
    onClick: () -> Unit = {},
) {
    val border = when {
        state == EpisodeWatchState.Watched -> null
        upNext && state == EpisodeWatchState.Unwatched -> BorderStroke(2.dp, TraktTheme.colors.accent)
        else -> BorderStroke(1.5.dp, TraktTheme.colors.textSecondary.copy(alpha = 0.4F))
    }

    Column(
        horizontalAlignment = CenterHorizontally,
        verticalArrangement = spacedBy(4.dp),
        modifier = modifier
            .alpha(if (state == EpisodeWatchState.Upcoming) 0.4F else 1F)
            .onClick(
                enabled = state != EpisodeWatchState.Upcoming && !busy,
                onClick = onClick,
            ),
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(32.dp)
                .background(
                    color = when (state) {
                        EpisodeWatchState.Watched -> TraktTheme.colors.accent
                        EpisodeWatchState.Unwatched, EpisodeWatchState.Upcoming -> Color.Transparent
                    },
                    shape = CircleShape,
                )
                .then(
                    border?.let { Modifier.border(it, CircleShape) } ?: Modifier,
                ),
        ) {
            if (busy) {
                FilmProgressIndicator(
                    size = 16.dp,
                    color = TraktTheme.colors.textSecondary,
                )
            } else {
                Icon(
                    painter = painterResource(R.drawable.ic_check),
                    tint = when (state) {
                        EpisodeWatchState.Watched -> TraktTheme.colors.textPrimaryOnAccent
                        EpisodeWatchState.Unwatched, EpisodeWatchState.Upcoming -> TraktTheme.colors.textSecondary
                    },
                    contentDescription = null,
                    modifier = Modifier.size(
                        when (state) {
                            EpisodeWatchState.Watched -> 16.dp
                            EpisodeWatchState.Unwatched, EpisodeWatchState.Upcoming -> 12.dp
                        },
                    ),
                )
            }
        }

        Text(
            text = number.toString(),
            style = TraktTheme.typography.meta,
            color = TraktTheme.colors.textPrimary,
        )
    }
}

@PreviewTraktLightDark
@Composable
private fun Preview() {
    TraktThemePreview {
        Row(
            horizontalArrangement = spacedBy(12.dp),
            modifier = Modifier.padding(16.dp),
        ) {
            EpisodeProgressChip(number = 1, state = EpisodeWatchState.Watched)
            EpisodeProgressChip(number = 2, state = EpisodeWatchState.Watched, busy = true)
            EpisodeProgressChip(number = 3, state = EpisodeWatchState.Unwatched, upNext = true)
            EpisodeProgressChip(number = 4, state = EpisodeWatchState.Unwatched)
            EpisodeProgressChip(number = 5, state = EpisodeWatchState.Upcoming)
        }
    }
}
