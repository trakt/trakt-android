package tv.trakt.trakt.common.helpers

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import kotlinx.coroutines.CoroutineScope

/**
 * The same as [LaunchedEffect] but skips the first invocation
 */
@Composable
fun LaunchedUpdateEffect(
    key: Any?,
    block: suspend CoroutineScope.() -> Unit,
) {
    var isTriggered by remember { mutableStateOf(false) }

    LaunchedEffect(key) {
        if (isTriggered) {
            block()
        } else {
            isTriggered = true
        }
    }
}
