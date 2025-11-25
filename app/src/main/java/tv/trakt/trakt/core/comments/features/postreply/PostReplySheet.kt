@file:OptIn(ExperimentalMaterial3Api::class)

package tv.trakt.trakt.core.comments.features.postreply

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SheetState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf
import tv.trakt.trakt.LocalSnackbarState
import tv.trakt.trakt.common.model.Comment
import tv.trakt.trakt.common.model.User
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.components.TraktBottomSheet
import tv.trakt.trakt.ui.snackbar.SNACK_DURATION_SHORT
import kotlin.random.Random

@Composable
internal fun PostReplySheet(
    state: SheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true,
    ),
    active: Boolean,
    comment: Comment?,
    user: User? = null,
    onReplyPost: (Comment) -> Unit,
    onDismiss: () -> Unit,
) {
    val localSnack = LocalSnackbarState.current
    val localContext = LocalContext.current

    val sheetScope = rememberCoroutineScope()

    if (active) {
        TraktBottomSheet(
            sheetState = state,
            onDismiss = onDismiss,
        ) {
            PostReplyView(
                viewModel = koinViewModel(
                    key = Random.nextInt().toString(),
                    parameters = {
                        parametersOf(comment, user)
                    },
                ),
                onReplyPost = {
                    onReplyPost(it)
                    sheetScope.dismissWithAction(
                        sheet = state,
                        onDismiss = onDismiss,
                        action = {
                            sheetScope.launch {
                                val job = sheetScope.launch {
                                    val message =
                                        localContext.getString(R.string.text_info_reply_added)
                                    localSnack.showSnackbar(message)
                                }
                                delay(SNACK_DURATION_SHORT)
                                job.cancel()
                            }
                        },
                    )
                },
                modifier = Modifier
                    .padding(horizontal = 24.dp)
                    .padding(bottom = 24.dp),
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
