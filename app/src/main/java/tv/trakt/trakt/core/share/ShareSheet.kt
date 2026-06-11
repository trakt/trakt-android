package tv.trakt.trakt.core.share

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SheetState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf
import tv.trakt.trakt.common.model.MediaType
import tv.trakt.trakt.common.model.SlugId
import tv.trakt.trakt.ui.components.TraktBottomSheet

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ShareSheet(
    state: SheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
    active: Boolean,
    mediaSlug: SlugId?,
    mediaType: MediaType?,
    onDismiss: () -> Unit,
) {
    if (active && mediaSlug != null && mediaType != null) {
        TraktBottomSheet(
            sheetState = state,
            onDismiss = onDismiss,
        ) {
            ShareView(
                viewModel = koinViewModel(
                    key = mediaSlug.value,
                    parameters = {
                        parametersOf(mediaSlug, mediaType)
                    },
                ),
                modifier = Modifier
                    .padding(bottom = 24.dp)
                    .padding(horizontal = 24.dp),
            )
        }
    }
}
