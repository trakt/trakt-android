package tv.trakt.trakt.core.calendar.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import tv.trakt.trakt.common.helpers.extensions.onClick
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.theme.TraktTheme

private val FadeDuration = tween<Float>(150)

@Composable
internal fun CalendarTodayIcon(
    visible: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
) {
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(FadeDuration),
        exit = fadeOut(FadeDuration),
        modifier = modifier,
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_calendar),
            tint = TraktTheme.colors.textPrimary,
            contentDescription = stringResource(R.string.button_text_reset_calendar_period),
            modifier = Modifier
                .size(22.dp)
                .onClick(onClick = onClick),
        )
    }
}
