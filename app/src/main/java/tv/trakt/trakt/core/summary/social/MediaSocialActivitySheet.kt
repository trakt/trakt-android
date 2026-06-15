@file:OptIn(ExperimentalMaterial3Api::class)

package tv.trakt.trakt.core.summary.social

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SheetState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.collections.immutable.ImmutableList
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf
import tv.trakt.trakt.common.model.User
import tv.trakt.trakt.core.summary.social.model.MediaSocialActivity
import tv.trakt.trakt.ui.components.TraktBottomSheet
import kotlin.random.Random.Default.nextInt

@Composable
internal fun MediaSocialActivitySheet(
    state: SheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true,
    ),
    activity: ImmutableList<MediaSocialActivity>?,
    mediaTitle: String,
    onUserClick: (user: User) -> Unit,
    onDismiss: () -> Unit,
) {
    val scope = rememberCoroutineScope()

    if (activity != null) {
        TraktBottomSheet(
            sheetState = state,
            onDismiss = onDismiss,
        ) {
            MediaSocialActivityView(
                viewModel = koinViewModel(
                    key = nextInt().toString(),
                    parameters = { parametersOf(activity) },
                ),
                mediaTitle = mediaTitle,
                onUserClick = {
                    scope.dismissWithAction(
                        sheet = state,
                        action = { onUserClick(it) },
                        onDismiss = onDismiss,
                    )
                },
                modifier = Modifier
                    .padding(bottom = 24.dp)
                    .padding(horizontal = 24.dp),
            )
        }
    }
}

private fun CoroutineScope.dismissWithAction(
    sheet: SheetState,
    action: () -> Unit = {},
    onDismiss: () -> Unit,
) {
    launch {
        sheet.hide()
    }.invokeOnCompletion {
        if (!sheet.isVisible) {
            action()
            onDismiss()
        }
    }
}
