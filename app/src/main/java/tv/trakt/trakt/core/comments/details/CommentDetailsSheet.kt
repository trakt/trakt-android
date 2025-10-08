@file:OptIn(ExperimentalMaterial3Api::class)

package tv.trakt.trakt.core.comments.details

import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SheetState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf
import tv.trakt.trakt.common.model.Comment
import tv.trakt.trakt.ui.components.TraktBottomSheet
import kotlin.random.Random.Default.nextInt

@Composable
internal fun CommentDetailsSheet(
    state: SheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = false,
    ),
    comment: Comment?,
    onDismiss: () -> Unit,
) {
    val sheetScope = rememberCoroutineScope()

    if (comment != null) {
        TraktBottomSheet(
            sheetState = state,
            onDismiss = onDismiss,
            modifier = Modifier,
        ) {
            CommentDetailsView(
                viewModel = koinViewModel(
                    key = nextInt().toString(),
                    parameters = { parametersOf(comment) },
                ),
                modifier = Modifier
                    .fillMaxHeight(0.9F)
                    .padding(horizontal = 24.dp),
            )
        }
    }
}
