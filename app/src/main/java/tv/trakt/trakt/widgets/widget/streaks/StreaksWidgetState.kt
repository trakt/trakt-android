package tv.trakt.trakt.widgets.widget.streaks

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

@Immutable
internal data class StreaksWidgetState(
    val streakDays: Int = 0,
    val week: ImmutableList<StreaksWidgetDay> = persistentListOf(),
    val loaded: Boolean = false,
    val error: Boolean = false,
)

@Immutable
internal data class StreaksWidgetDay(
    val active: Boolean,
    val today: Boolean,
    val future: Boolean,
)
