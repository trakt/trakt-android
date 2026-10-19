package tv.trakt.trakt.core.main.ui.inputhold

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hasRoute
import kotlinx.coroutines.delay
import tv.trakt.trakt.core.today.stories.navigation.TodayStoriesDestination
import kotlin.time.Duration.Companion.milliseconds

private const val STORIES_EXIT_INPUT_HOLD_MS = 500L

/**
 * Swallows taps briefly after leaving Today stories, so rapid story taps
 * don't land on the screen and bottom bar revealed underneath.
 */
@Composable
internal fun MainInputHoldView(
    destination: NavDestination?,
    modifier: Modifier = Modifier,
) {
    var inputHold by remember { mutableStateOf(false) }
    var previousDestination by remember { mutableStateOf<NavDestination?>(null) }

    LaunchedEffect(destination) {
        val leftStories = previousDestination?.hasRoute(TodayStoriesDestination::class) == true &&
            destination?.hasRoute(TodayStoriesDestination::class) != true
        previousDestination = destination
        if (!leftStories) {
            return@LaunchedEffect
        }

        inputHold = true
        try {
            delay(STORIES_EXIT_INPUT_HOLD_MS.milliseconds)
        } finally {
            inputHold = false
        }
    }

    if (inputHold) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    awaitPointerEventScope {
                        while (true) {
                            awaitPointerEvent().changes.forEach { it.consume() }
                        }
                    }
                },
        )
    }
}
