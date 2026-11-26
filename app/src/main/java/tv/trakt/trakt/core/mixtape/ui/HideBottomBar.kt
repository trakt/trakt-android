package tv.trakt.trakt.core.mixtape.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import tv.trakt.trakt.LocalBottomBarVisibility

/**
 * Hides the bottom bar while shown. Re-hides it when the screen it replaced restores it on dispose,
 * since the incoming screen's effect can run before the outgoing screen's onDispose.
 */
@Composable
internal fun HideBottomBar() {
    val visibility = LocalBottomBarVisibility.current

    LaunchedEffect(visibility.value) {
        if (visibility.value) visibility.value = false
    }

    DisposableEffect(Unit) {
        visibility.value = false
        onDispose {
            visibility.value = true
        }
    }
}
