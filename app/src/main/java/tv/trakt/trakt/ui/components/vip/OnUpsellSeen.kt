package tv.trakt.trakt.ui.components.vip

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.currentStateAsState
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.seconds

private val SEEN_DURATION = 1.seconds

// A full view can round to a hair under the measured size.
private const val FULLY_VISIBLE_RATIO = 0.99F

/**
 * Calls [onSeen] once, after the element stayed fully on screen for one second
 * while the screen was resumed.
 */
@Composable
internal fun Modifier.onUpsellSeen(onSeen: () -> Unit): Modifier {
    var isFullyVisible by remember { mutableStateOf(false) }
    var isSeen by remember { mutableStateOf(false) }
    val currentOnSeen by rememberUpdatedState(onSeen)
    val lifecycleState by LocalLifecycleOwner.current.lifecycle.currentStateAsState()
    val isResumed = lifecycleState.isAtLeast(Lifecycle.State.RESUMED)

    LaunchedEffect(isFullyVisible, isResumed, isSeen) {
        if (!isFullyVisible || !isResumed || isSeen) return@LaunchedEffect
        delay(SEEN_DURATION)
        isSeen = true
        currentOnSeen()
    }

    return onGloballyPositioned { coordinates ->
        val size = coordinates.size
        val visible = coordinates.boundsInWindow()
        isFullyVisible = size.width > 0 &&
            size.height > 0 &&
            visible.width >= size.width * FULLY_VISIBLE_RATIO &&
            visible.height >= size.height * FULLY_VISIBLE_RATIO
    }
}
