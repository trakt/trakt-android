package tv.trakt.trakt.common.helpers.extensions

import androidx.compose.foundation.clickable
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import java.lang.System.currentTimeMillis

fun Modifier.ifOrElse(
    condition: Boolean,
    trueModifier: Modifier,
    falseModifier: Modifier = Modifier,
): Modifier = then(if (condition) trueModifier else falseModifier)

fun Modifier.onClick(
    enabled: Boolean = true,
    onClick: () -> Unit,
): Modifier {
    return composed {
        val lastClickMs = remember { mutableLongStateOf(0L) }
        clickable(
            enabled = enabled,
            indication = null,
            interactionSource = null,
            onClick = {
                val currentTime = currentTimeMillis()
                if (currentTime - lastClickMs.longValue > 350) {
                    lastClickMs.longValue = currentTime
                    onClick()
                }
            },
        )
    }
}
