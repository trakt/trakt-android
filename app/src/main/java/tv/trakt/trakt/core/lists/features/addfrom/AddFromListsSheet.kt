@file:OptIn(ExperimentalMaterial3Api::class)

package tv.trakt.trakt.core.lists.features.addfrom

import android.content.res.Resources
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
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf
import tv.trakt.trakt.LocalSnackbarState
import tv.trakt.trakt.common.core.lists.usecases.toUserMessage
import tv.trakt.trakt.common.model.lists.CustomList
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.components.TraktBottomSheet
import kotlin.random.Random.Default.nextInt

@Composable
internal fun AddFromListsSheet(
    state: SheetState = rememberBottomSheetState(
        initialValue = Hidden,
        enabledValues = setOf(Hidden, Expanded),
    ),
    list: CustomList?,
    onDismiss: () -> Unit,
) {
    val sheetScope = rememberCoroutineScope()
    val viewModelKey = remember(list) { nextInt().toString() }
    val localSnack = LocalSnackbarState.current
    val localResources = LocalResources.current

    if (list != null) {
        TraktBottomSheet(
            sheetState = state,
            onDismiss = onDismiss,
        ) {
            AddFromListsView(
                listName = list.name,
                viewModel = koinViewModel(
                    key = viewModelKey,
                    parameters = { parametersOf(list) },
                ),
                onResult = { result ->
                    sheetScope.launch {
                        state.hide()
                        onDismiss()
                    }
                    sheetScope.launch {
                        localSnack.showSnackbar(
                            message = result.toMessage(localResources, list.name),
                            duration = SnackbarDuration.Short,
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

private fun AddFromListsResult.toMessage(
    resources: Resources,
    listName: String,
): String {
    return when (this) {
        is AddFromListsResult.Added -> when (count) {
            1 -> resources.getString(R.string.text_added_title_to_list, listName)
            else -> resources.getString(R.string.text_added_titles_to_list, count.toString(), listName)
        }
        is AddFromListsResult.LimitReached -> error.toUserMessage(resources)
        is AddFromListsResult.Failed -> resources.getString(R.string.text_failed_to_add_titles, listName)
    }
}
