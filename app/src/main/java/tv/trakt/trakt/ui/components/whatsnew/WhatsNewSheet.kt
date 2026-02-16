@file:OptIn(ExperimentalMaterial3Api::class)

package tv.trakt.trakt.ui.components.whatsnew

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SheetState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import tv.trakt.trakt.common.model.WhatsNew
import tv.trakt.trakt.ui.components.TraktBottomSheet

@Composable
internal fun WhatsNewSheet(
    state: SheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true,
    ),
    data: WhatsNew? = null,
    onDismiss: () -> Unit = {},
) {
    val sheetScope = rememberCoroutineScope()

    if (data != null) {
        TraktBottomSheet(
            sheetState = state,
            onDismiss = onDismiss,
        ) {
            WhatsNewView(
                data = data,
                onDismiss = {
                    sheetScope.launch {
                        state.hide()
                    }.invokeOnCompletion {
                        if (!state.isVisible) {
                            onDismiss()
                        }
                    }
                },
                modifier = Modifier
                    .padding(horizontal = 24.dp)
                    .padding(top = 4.dp, bottom = 24.dp),
            )
        }
    }
}
