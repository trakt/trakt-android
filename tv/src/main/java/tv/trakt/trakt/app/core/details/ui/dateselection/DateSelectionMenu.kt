package tv.trakt.trakt.app.core.details.ui.dateselection

import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import tv.trakt.trakt.app.common.ui.menus.TvDropdownMenu
import tv.trakt.trakt.app.common.ui.menus.TvDropdownMenuItem
import tv.trakt.trakt.common.model.DateSelectionResult
import tv.trakt.trakt.common.model.Now
import tv.trakt.trakt.common.model.ReleaseDate
import tv.trakt.trakt.common.model.UnknownDate
import tv.trakt.trakt.resources.R

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
internal fun DateSelectionMenu(
    expanded: Boolean,
    onDismissRequest: () -> Unit,
    onSelect: (DateSelectionResult) -> Unit,
    modifier: Modifier = Modifier,
) {
    TvDropdownMenu(
        visible = expanded,
        onDismiss = onDismissRequest,
        modifier = modifier,
    ) {
        var focusedIndex by remember { mutableIntStateOf(0) }

        TvDropdownMenuItem(
            text = stringResource(R.string.button_text_mark_as_watched_now),
            icon = painterResource(R.drawable.ic_check_double),
            focused = focusedIndex == 0,
            onClick = {
                onDismissRequest()
                onSelect(Now)
            },
            modifier = Modifier.onFocusChanged {
                if (it.isFocused) focusedIndex = 0
            },
        )

        TvDropdownMenuItem(
            text = stringResource(R.string.button_text_mark_as_watched_release_date),
            icon = painterResource(R.drawable.ic_calendar_time_trakt),
            focused = focusedIndex == 1,
            onClick = {
                onDismissRequest()
                onSelect(ReleaseDate)
            },
            modifier = Modifier.onFocusChanged {
                if (it.isFocused) focusedIndex = 1
            },
        )

        TvDropdownMenuItem(
            text = stringResource(R.string.button_text_mark_as_watched_unknown_date),
            icon = painterResource(R.drawable.ic_question),
            focused = focusedIndex == 2,
            onClick = {
                onDismissRequest()
                onSelect(UnknownDate)
            },
            modifier = Modifier.onFocusChanged {
                if (it.isFocused) focusedIndex = 2
            },
        )
    }
}
