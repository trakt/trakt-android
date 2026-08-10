package tv.trakt.trakt.widgets.calendar

import android.graphics.Bitmap
import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import tv.trakt.trakt.widgets.WidgetIntentTarget

@Immutable
internal data class CalendarWidgetState(
    val days: ImmutableList<CalendarWidgetDay> = persistentListOf(),
    val error: Boolean = false,
)

@Immutable
internal data class CalendarWidgetDay(
    val label: String,
    val isToday: Boolean,
    val items: ImmutableList<CalendarWidgetItem>,
)

@Immutable
internal data class CalendarWidgetItem(
    val key: String,
    val title: String,
    val subtitle: String,
    val image: Bitmap?,
    val timeText: String?,
    val tag: CalendarWidgetTag?,
    val watched: Boolean,
    val isMovie: Boolean,
    val imageTarget: WidgetIntentTarget,
    val titleTarget: WidgetIntentTarget,
)

internal enum class CalendarWidgetTag {
    Premiere,
    Finale,
}
