package tv.trakt.trakt.core.home.sections.activity.sheets

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import tv.trakt.trakt.LocalSnackbarState
import tv.trakt.trakt.core.home.sections.activity.model.HomeActivityItem
import tv.trakt.trakt.core.home.sections.activity.views.context.ActivityItemContextView
import tv.trakt.trakt.ui.components.TraktBottomSheet

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun HomeActivityItemSheet(
    sheetItem: HomeActivityItem?,
    onDismiss: () -> Unit,
) {
    val localSnack = LocalSnackbarState.current
    val localContext = LocalContext.current

    val sheetScope = rememberCoroutineScope()

    if (sheetItem != null) {
        TraktBottomSheet(
            onDismiss = onDismiss,
        ) {
            ActivityItemContextView(
                item = sheetItem,
                onRemoveWatchedClick = onDismiss,
                modifier = Modifier
                    .padding(top = 4.dp)
                    .padding(bottom = 24.dp)
                    .padding(horizontal = 24.dp),
            )
        }
    }
}
