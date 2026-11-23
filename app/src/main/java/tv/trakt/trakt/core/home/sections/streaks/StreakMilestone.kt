package tv.trakt.trakt.core.home.sections.streaks

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

private val StreakMilestones = setOf(7, 30, 100, 365)

internal fun crossedStreakMilestone(
    previous: Int,
    next: Int,
): Int? {
    if (next != previous + 1) return null
    return next.takeIf { it in StreakMilestones }
}

@Composable
internal fun rememberStreakCelebration(streakTotal: Int?): Int {
    var previous by remember { mutableStateOf<Int?>(null) }
    var celebration by remember { mutableIntStateOf(0) }

    LaunchedEffect(streakTotal) {
        val before = previous
        if (before != null && streakTotal != null && crossedStreakMilestone(before, streakTotal) != null) {
            celebration++
        }
        previous = streakTotal
    }

    return celebration
}
