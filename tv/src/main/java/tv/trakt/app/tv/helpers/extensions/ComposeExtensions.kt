package tv.trakt.app.tv.helpers.extensions

import androidx.compose.foundation.focusable
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.focus.focusProperties
import tv.trakt.app.tv.common.ui.mediacards.HorizontalMediaSkeletonCard

internal fun LazyListScope.emptyFocusListItems() {
    items(count = 8) {
        HorizontalMediaSkeletonCard(
            modifier = Modifier
                .alpha(0F)
                .focusProperties { canFocus = false }
                .focusable(false),
        )
    }
}
