package tv.trakt.trakt.ui.extensions

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.snapshotFlow
import kotlinx.coroutines.flow.first

internal suspend fun LazyListState.scrollToItemIfHidden(index: Int) {
    val layout = snapshotFlow { layoutInfo }.first { it.totalItemsCount > 0 }
    val item = layout.visibleItemsInfo.firstOrNull { it.index == index }
    val isFullyVisible = item != null &&
        item.offset >= layout.viewportStartOffset &&
        item.offset + item.size <= layout.viewportEndOffset

    if (isFullyVisible) return
    scrollToItem(index)
}
