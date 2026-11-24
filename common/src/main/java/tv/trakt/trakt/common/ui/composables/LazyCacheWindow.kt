package tv.trakt.trakt.common.ui.composables

import androidx.compose.foundation.lazy.layout.LazyLayoutCacheWindow
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember

@Composable
fun rememberCacheWindow(fraction: Float): LazyLayoutCacheWindow =
    remember(fraction) {
        LazyLayoutCacheWindow(
            aheadFraction = fraction,
            behindFraction = fraction,
        )
    }
