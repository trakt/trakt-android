package tv.trakt.trakt.common.helpers.extensions

import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.material3.ripple
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
    throttle: Boolean = true,
    onClick: () -> Unit,
): Modifier {
    return composed {
        val lastClickMs = remember { mutableLongStateOf(0L) }
        val delaysMs = remember(throttle) { if (throttle) 350L else 0L }
        clickable(
            enabled = enabled,
            indication = null,
            interactionSource = null,
            onClick = {
                val currentTime = currentTimeMillis()
                if (currentTime - lastClickMs.longValue > delaysMs) {
                    lastClickMs.longValue = currentTime
                    onClick()
                }
            },
        )
    }
}

fun Modifier.onClickCombined(
    enabled: Boolean = true,
    throttle: Boolean = true,
    onLongClick: (() -> Unit)? = null,
    onClick: () -> Unit,
): Modifier {
    return composed {
        val lastClickMs = remember { mutableLongStateOf(0L) }
        val delaysMs = remember(throttle) { if (throttle) 350L else 0L }
        combinedClickable(
            enabled = enabled,
            onClick = {
                val currentTime = currentTimeMillis()
                if (currentTime - lastClickMs.longValue > delaysMs) {
                    lastClickMs.longValue = currentTime
                    onClick()
                }
            },
            onLongClick = onLongClick?.let {
                {
                    val currentTime = currentTimeMillis()
                    if (currentTime - lastClickMs.longValue > delaysMs) {
                        lastClickMs.longValue = currentTime
                        onLongClick()
                    }
                }
            },
            interactionSource = remember { MutableInteractionSource() },
            indication = ripple(),
        )
    }
}
