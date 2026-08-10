package tv.trakt.trakt.widgets.streaks

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

internal data class StreaksWidgetState(
    val streakDays: Int = 0,
    val week: ImmutableList<StreaksWidgetDay> = persistentListOf(),
    val loaded: Boolean = false,
    val error: Boolean = false,
)

internal data class StreaksWidgetDay(
    val active: Boolean,
    val today: Boolean,
    val future: Boolean,
)
