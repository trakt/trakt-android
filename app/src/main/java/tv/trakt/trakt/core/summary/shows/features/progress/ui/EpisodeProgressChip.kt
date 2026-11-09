package tv.trakt.trakt.core.summary.shows.features.progress.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
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
import tv.trakt.trakt.common.ui.theme.colors.Shade920
import tv.trakt.trakt.core.summary.shows.features.progress.ui.EpisodeWatchState.Unwatched
import tv.trakt.trakt.core.summary.shows.features.progress.ui.EpisodeWatchState.Upcoming
import tv.trakt.trakt.core.summary.shows.features.progress.ui.EpisodeWatchState.Watched
import tv.trakt.trakt.helpers.extensions.TraktThemePreview
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.theme.TraktTheme
import tv.trakt.trakt.ui.theme.model.SeasonalTheme

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
        state == Watched -> null
        upNext && state == Unwatched -> BorderStroke(1.dp, TraktTheme.colors.accent)
        else -> BorderStroke(1.dp, TraktTheme.colors.textPrimary)
    }

    val contentColor = when (state) {
        Watched -> when (TraktTheme.colors.isLight) {
            true -> TraktTheme.colors.textPrimaryOnAccent
            false -> Shade920
        }
        Unwatched, Upcoming -> TraktTheme.colors.textPrimary
    }

    Column(
        horizontalAlignment = CenterHorizontally,
        verticalArrangement = spacedBy(4.dp),
        modifier = modifier
            .alpha(if (state == Upcoming) 0.33F else 1F)
            .onClick(
                enabled = state != Upcoming && !busy,
                onClick = onClick,
            ),
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(32.dp)
                .background(
                    color = when (state) {
                        Watched -> TraktTheme.colors.textPrimary
                        Unwatched, Upcoming -> Color.Transparent
                    },
                    shape = CircleShape,
                )
                .then(
                    border?.let { Modifier.border(it, CircleShape) } ?: Modifier,
                ),
        ) {
            if (busy) {
                FilmProgressIndicator(
                    size = 14.dp,
                    color = contentColor,
                )
            } else {
                Icon(
                    painter = painterResource(
                        when (state) {
                            Watched -> R.drawable.ic_check_double
                            Unwatched, Upcoming -> R.drawable.ic_check_2
                        },
                    ),
                    tint = contentColor,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
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
        PreviewChips()
    }
}

@PreviewTraktLightDark
@Composable
private fun SeasonalPreview() {
    val dark = isSystemInDarkTheme()

    Column {
        SeasonalTheme.entries.forEach { season ->
            TraktTheme(colors = if (dark) season.darkColors else season.lightColors) {
                PreviewChips(
                    modifier = Modifier.background(TraktTheme.colors.backgroundPrimary),
                )
            }
        }
    }
}

@Composable
private fun PreviewChips(modifier: Modifier = Modifier) {
    Row(
        horizontalArrangement = spacedBy(12.dp),
        modifier = modifier.padding(16.dp),
    ) {
        EpisodeProgressChip(number = 1, state = Watched)
        EpisodeProgressChip(number = 2, state = Watched, busy = true)
        EpisodeProgressChip(number = 3, state = Unwatched, upNext = true)
        EpisodeProgressChip(number = 4, state = Unwatched)
        EpisodeProgressChip(number = 6, state = Unwatched, busy = true)
        EpisodeProgressChip(number = 5, state = Upcoming)
    }
}
