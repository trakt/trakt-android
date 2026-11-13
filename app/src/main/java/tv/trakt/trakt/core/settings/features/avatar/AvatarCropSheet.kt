package tv.trakt.trakt.core.settings.features.avatar

import android.net.Uri
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
import kotlinx.coroutines.launch
import tv.trakt.trakt.ui.components.TraktBottomSheet

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun AvatarCropSheet(
    state: SheetState = rememberBottomSheetState(
        initialValue = Hidden,
        enabledValues = setOf(Hidden, Expanded),
    ),
    image: Uri?,
    onConfirm: (Uri, AvatarCrop) -> Unit,
    onDismiss: () -> Unit,
) {
    val sheetScope = rememberCoroutineScope()

    if (image != null) {
        TraktBottomSheet(
            sheetState = state,
            onDismiss = onDismiss,
        ) {
            AvatarCropView(
                image = image,
                onConfirm = { crop ->
                    onConfirm(image, crop)
                    sheetScope.launch { state.hide() }
                        .invokeOnCompletion {
                            if (!state.isVisible) {
                                onDismiss()
                            }
                        }
                },
                onDismiss = {
                    sheetScope.launch { state.hide() }
                        .invokeOnCompletion {
                            if (!state.isVisible) {
                                onDismiss()
                            }
                        }
                },
                modifier = Modifier
                    .padding(horizontal = 24.dp)
                    .padding(bottom = 24.dp),
            )
        }
    }
}
