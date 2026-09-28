package tv.trakt.trakt.core.lists.features.reorder.lists

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.collections.immutable.toImmutableList
import tv.trakt.trakt.core.lists.features.reorder.ui.ReorderActions
import tv.trakt.trakt.core.lists.features.reorder.ui.ReorderItem
import tv.trakt.trakt.core.lists.features.reorder.ui.ReorderLayout
import tv.trakt.trakt.core.lists.features.reorder.ui.ReorderUiState
import tv.trakt.trakt.resources.R

@Composable
internal fun ListsReorderScreen(
    modifier: Modifier = Modifier,
    viewModel: ListsReorderViewModel,
    onNavigateBack: () -> Unit,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    val resources = LocalResources.current
    val title = stringResource(R.string.list_title_personal_lists)

    val uiState = remember(state, title) {
        ReorderUiState(
            title = title,
            items = state.items
                ?.map { list ->
                    ReorderItem(
                        key = list.ids.trakt.value.toString(),
                        title = list.name,
                        subtitle = list.privacy
                            ?.let { resources.getString(it.displayRes) }
                            .orEmpty(),
                        posterUrl = list.images?.getPostersUrl()?.firstOrNull(),
                        backdropUrl = null,
                    )
                }
                ?.toImmutableList(),
            changed = state.items != null &&
                state.items?.map { it.ids.trakt } != state.initialItemsOrder,
            loading = state.loading.isLoading,
            error = state.error,
            done = state.done,
        )
    }

    val actions = remember(viewModel) {
        ReorderActions(
            onMove = viewModel::reorderItem,
            onMoveToTop = viewModel::moveToTop,
            onMoveToBottom = viewModel::moveToBottom,
            onMoveToPosition = { index, position ->
                viewModel.moveToPosition(index = index, position = position)
            },
            onApply = viewModel::applyChanges,
            onErrorShown = viewModel::clearError,
        )
    }

    ReorderLayout(
        state = uiState,
        actions = actions,
        modifier = modifier,
        onNavigateBack = onNavigateBack,
    )
}
