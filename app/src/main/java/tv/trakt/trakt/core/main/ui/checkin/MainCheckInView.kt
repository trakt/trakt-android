package tv.trakt.trakt.core.main.ui.checkin

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import tv.trakt.trakt.LocalCheckInVisibility
import tv.trakt.trakt.core.checkin.model.CheckInState
import tv.trakt.trakt.core.checkin.model.expiresAt
import tv.trakt.trakt.core.checkin.model.image
import tv.trakt.trakt.core.checkin.model.startedAt
import tv.trakt.trakt.core.checkin.model.title
import tv.trakt.trakt.core.checkin.ui.CheckInView
import tv.trakt.trakt.core.main.MainState
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.theme.TraktTheme

@Composable
internal fun ColumnScope.MainCheckInView(
    state: MainState,
    onMediaClick: () -> Unit = {},
    onDismiss: () -> Unit = {},
) {
    val localCheckInVisibility = LocalCheckInVisibility.current

    var isExpired by remember(state.checkIn) {
        mutableStateOf(false)
    }

    var isExpanded by rememberSaveable {
        mutableStateOf(true)
    }
    val isVisible = remember(state.checkIn, isExpired) {
        state.checkIn?.isActive() == true && !isExpired
    }

    AnimatedVisibility(
        visible = isVisible && localCheckInVisibility.value,
        enter = fadeIn(tween(250)) + slideInVertically(initialOffsetY = { it / 10 }),
        exit = fadeOut(tween(100)),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = TraktTheme.spacing.mainPageHorizontalSpace - 8.dp),
    ) {
        CheckInView(
            title = state.checkIn?.title,
            subtitle = when (state.checkIn) {
                is CheckInState.ActiveMovie -> stringResource(R.string.translated_value_type_movie)
                is CheckInState.ActiveEpisode -> state.checkIn.episode.seasonEpisodeString()
                else -> null
            },
            image = state.checkIn?.image,
            startedAt = state.checkIn?.startedAt,
            expiresAt = state.checkIn?.expiresAt,
            expanded = isExpanded,
            onMediaClick = onMediaClick,
            onCollapseClick = {
                isExpanded = !isExpanded
            },
            onExpire = {
                isExpired = true
            },
            onDismiss = onDismiss,
        )
    }
}
