@file:OptIn(ExperimentalMaterial3Api::class)

package tv.trakt.trakt.core.calendar.feature.monthly.sheets

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SheetState
import androidx.compose.material3.SheetValue.Expanded
import androidx.compose.material3.SheetValue.Hidden
import androidx.compose.material3.rememberBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableSet
import kotlinx.coroutines.launch
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.core.calendar.model.CalendarItem
import tv.trakt.trakt.core.calendar.model.CalendarItem.EpisodeItem
import tv.trakt.trakt.core.calendar.model.CalendarItem.MovieItem
import tv.trakt.trakt.ui.components.TraktBottomSheet
import java.time.LocalDate

@Composable
internal fun CalendarDaySheet(
    visible: Boolean,
    date: LocalDate?,
    state: SheetState = rememberBottomSheetState(
        initialValue = Hidden,
        enabledValues = setOf(Hidden, Expanded),
    ),
    items: ImmutableList<CalendarItem>? = null,
    itemsLoading: ImmutableSet<TraktId>? = null,
    onEpisodeClick: (EpisodeItem) -> Unit = {},
    onShowClick: (EpisodeItem) -> Unit = {},
    onMovieClick: (MovieItem) -> Unit = {},
    onCheckClick: (CalendarItem) -> Unit = {},
    onCheckLongClick: (CalendarItem) -> Unit = {},
    onRemoveClick: (CalendarItem) -> Unit = {},
    onDismiss: () -> Unit = {},
) {
    if (!visible || date == null) {
        return
    }

    val sheetScope = rememberCoroutineScope()

    fun dismissThen(action: () -> Unit) {
        sheetScope.launch { state.hide() }
            .invokeOnCompletion {
                if (!state.isVisible) {
                    onDismiss()
                }
                action()
            }
    }

    TraktBottomSheet(
        sheetState = state,
        onDismiss = onDismiss,
    ) {
        CalendarDayView(
            date = date,
            items = items,
            itemsLoading = itemsLoading,
            onEpisodeClick = { item -> dismissThen { onEpisodeClick(item) } },
            onShowClick = { item -> dismissThen { onShowClick(item) } },
            onMovieClick = { item -> dismissThen { onMovieClick(item) } },
            onCheckClick = onCheckClick,
            onCheckLongClick = onCheckLongClick,
            onRemoveClick = onRemoveClick,
            modifier = Modifier
                .padding(horizontal = 24.dp)
                .padding(top = 4.dp, bottom = 16.dp),
        )
    }
}
