package tv.trakt.trakt.core.lists.features.reorder.ui

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList

@Immutable
internal data class ReorderItem(
    val key: String,
    val title: String,
    val subtitle: String,
    val posterUrl: String?,
    val backdropUrl: String?,
)

@Immutable
internal data class ReorderUiState(
    val title: String = "",
    val items: ImmutableList<ReorderItem>? = null,
    val changed: Boolean = false,
    val loading: Boolean = false,
    val error: Exception? = null,
    val done: Boolean = false,
)

internal data class ReorderActions(
    val onMove: (from: Int, to: Int) -> Unit = { _, _ -> },
    val onMoveToTop: (index: Int) -> Unit = {},
    val onMoveToBottom: (index: Int) -> Unit = {},
    val onMoveToPosition: (index: Int, position: Int) -> Unit = { _, _ -> },
    val onApply: () -> Unit = {},
    val onErrorShown: () -> Unit = {},
)
