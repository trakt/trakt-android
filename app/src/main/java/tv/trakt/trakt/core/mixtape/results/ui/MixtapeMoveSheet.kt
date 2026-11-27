package tv.trakt.trakt.core.mixtape.results.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SheetState
import androidx.compose.material3.SheetValue.Expanded
import androidx.compose.material3.SheetValue.Hidden
import androidx.compose.material3.rememberBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.collections.immutable.ImmutableList
import kotlinx.coroutines.launch
import tv.trakt.trakt.core.mixtape.model.MixtapeTarget
import tv.trakt.trakt.core.mixtape.model.MixtapeTarget.PersonalList
import tv.trakt.trakt.ui.components.TraktBottomSheet

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun MixtapeMoveSheet(
    state: SheetState = rememberBottomSheetState(
        initialValue = Hidden,
        enabledValues = setOf(Hidden, Expanded),
    ),
    active: Boolean,
    current: MixtapeTarget,
    lists: ImmutableList<PersonalList>,
    suggestedName: String,
    moving: Boolean,
    onMove: (MixtapeTarget) -> Unit,
    onMoveNew: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    val sheetScope = rememberCoroutineScope()

    fun hide() {
        sheetScope
            .launch { state.hide() }
            .invokeOnCompletion {
                if (!state.isVisible) onDismiss()
            }
    }

    if (active) {
        TraktBottomSheet(
            sheetState = state,
            onDismiss = onDismiss,
        ) {
            MixtapeMoveView(
                current = current,
                lists = lists,
                suggestedName = suggestedName,
                moving = moving,
                onStay = ::hide,
                onMove = {
                    onMove(it)
                    hide()
                },
                onMoveNew = {
                    onMoveNew(it)
                    hide()
                },
                modifier = Modifier
                    .padding(bottom = 24.dp)
                    .padding(horizontal = 24.dp),
            )
        }
    }
}
