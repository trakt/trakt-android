@file:OptIn(ExperimentalMaterial3Api::class)

package tv.trakt.trakt.core.lists.features.manage

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SheetState
import androidx.compose.material3.SheetValue.Expanded
import androidx.compose.material3.SheetValue.Hidden
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.rememberBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf
import tv.trakt.trakt.LocalSnackbarState
import tv.trakt.trakt.common.model.lists.CustomList
import tv.trakt.trakt.ui.components.TraktBottomSheet
import kotlin.random.Random.Default.nextInt

@Composable
internal fun ManageListsSheet(
    state: SheetState = rememberBottomSheetState(
        initialValue = Hidden,
        enabledValues = setOf(Hidden, Expanded),
    ),
    target: ManageListsTarget?,
    inWatchlist: Boolean = false,
    onWatchlistClick: (() -> Unit)? = null,
    onWatchlistOpenClick: (() -> Unit)? = null,
    onListClick: ((CustomList) -> Unit)? = null,
    onDismiss: () -> Unit,
) {
    val sheetScope = rememberCoroutineScope()
    val viewModelKey = remember(target) { nextInt().toString() }
    val localSnack = LocalSnackbarState.current

    if (target != null) {
        TraktBottomSheet(
            sheetState = state,
            onDismiss = onDismiss,
        ) {
            ManageListsView(
                target = target,
                viewModel = koinViewModel(
                    key = viewModelKey,
                    parameters = { parametersOf(target) },
                ),
                inWatchlist = inWatchlist,
                onWatchlistClick = onWatchlistClick?.let { onClick ->
                    {
                        onClick()
                        sheetScope.launch {
                            state.hide()
                            onDismiss()
                        }
                    }
                },
                onWatchlistOpen = onWatchlistOpenClick?.let { onClick ->
                    {
                        sheetScope.launch {
                            state.hide()
                            onDismiss()
                            onClick()
                        }
                    }
                },
                onListOpen = onListClick?.let { onClick ->
                    { list ->
                        sheetScope.launch {
                            state.hide()
                            onDismiss()
                            onClick(list)
                        }
                    }
                },
                onItemLimit = { message ->
                    sheetScope.launch {
                        state.hide()
                        onDismiss()
                    }
                    sheetScope.launch {
                        localSnack.showSnackbar(
                            message = message,
                            duration = SnackbarDuration.Long,
                        )
                    }
                },
                modifier = Modifier
                    .padding(bottom = 24.dp)
                    .padding(horizontal = 24.dp),
            )
        }
    }
}
