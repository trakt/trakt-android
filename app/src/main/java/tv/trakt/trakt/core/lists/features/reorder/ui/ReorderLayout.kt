package tv.trakt.trakt.core.lists.features.reorder.ui

import androidx.activity.compose.BackHandler
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SnackbarDuration.Long
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import tv.trakt.trakt.LocalSnackbarState
import tv.trakt.trakt.common.ui.theme.colors.Red500
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.components.confirmation.ConfirmationSheet
import tv.trakt.trakt.ui.components.input.SingleInputSheet

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ReorderLayout(
    state: ReorderUiState,
    actions: ReorderActions,
    modifier: Modifier = Modifier,
    onNavigateBack: () -> Unit,
) {
    val snack = LocalSnackbarState.current

    var showExitConfirm by remember { mutableStateOf(false) }
    var positionInputIndex by remember { mutableStateOf<Int?>(null) }

    LaunchedEffect(state.error) {
        if (state.error != null) {
            snack.showSnackbar(
                message = state.error.localizedMessage ?: "",
                duration = Long,
            )
            actions.onErrorShown()
        }
    }

    LaunchedEffect(state.done) {
        if (state.done) {
            onNavigateBack()
        }
    }

    val handleBack = {
        if (state.changed) {
            showExitConfirm = true
        } else {
            onNavigateBack()
        }
    }

    BackHandler(enabled = state.changed) {
        showExitConfirm = true
    }

    ReorderContent(
        state = state,
        modifier = modifier,
        onMove = actions.onMove,
        onMoveToTop = actions.onMoveToTop,
        onMoveToBottom = actions.onMoveToBottom,
        onMoveToPosition = { positionInputIndex = it },
        onApplyClick = actions.onApply,
        onBackClick = handleBack,
    )

    val positionItemTitle = positionInputIndex
        ?.let { state.items?.getOrNull(it)?.title }
        .orEmpty()

    SingleInputSheet(
        active = positionInputIndex != null,
        title = stringResource(R.string.button_text_move_to_position),
        description = stringResource(
            R.string.dialog_prompt_move_to_position,
            positionItemTitle,
        ),
        initialInput = positionInputIndex?.let { (it + 1).toString() },
        type = KeyboardType.Number,
        onApply = { input ->
            val index = positionInputIndex
            val position = input?.trim()?.toIntOrNull()
            if (index != null && position != null) {
                actions.onMoveToPosition(index, position)
            }
        },
        onDismiss = { positionInputIndex = null },
    )

    ConfirmationSheet(
        active = showExitConfirm,
        title = stringResource(R.string.dialog_title_discard_changes),
        message = stringResource(R.string.warning_prompt_discard_changes),
        yesText = stringResource(R.string.button_text_discard),
        yesColor = Red500,
        onYes = {
            showExitConfirm = false
            onNavigateBack()
        },
        onNo = {
            showExitConfirm = false
        },
    )
}
