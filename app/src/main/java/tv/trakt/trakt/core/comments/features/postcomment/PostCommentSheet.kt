@file:OptIn(ExperimentalMaterial3Api::class)

package tv.trakt.trakt.core.comments.features.postcomment

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf
import tv.trakt.trakt.LocalSnackbarState
import tv.trakt.trakt.common.helpers.extensions.toGifQuery
import tv.trakt.trakt.common.model.Comment
import tv.trakt.trakt.common.model.MediaType
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.core.comments.model.MentionSource
import tv.trakt.trakt.core.comments.ui.rememberDiscardGuardedSheetState
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.components.TraktBottomSheet
import tv.trakt.trakt.ui.components.confirmation.RemoveConfirmationSheet
import tv.trakt.trakt.ui.snackbar.ShortSnackDuration
import kotlin.random.Random

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun PostCommentSheet(
    active: Boolean,
    mediaId: TraktId?,
    mediaType: MediaType?,
    mediaTitle: String? = null,
    mentionSource: MentionSource? = null,
    onCommentPost: (Comment) -> Unit,
    onDismiss: () -> Unit,
) {
    val localSnack = LocalSnackbarState.current
    val localRes = LocalResources.current

    val sheetScope = rememberCoroutineScope()

    var hasChanges by remember(active) { mutableStateOf(false) }
    var isDiscardConfirmVisible by remember(active) { mutableStateOf(false) }

    val viewModelKey = remember(active) { Random.nextInt().toString() }

    val state = rememberDiscardGuardedSheetState(
        hasChanges = { hasChanges },
        onDiscardRequest = { isDiscardConfirmVisible = true },
    )

    if (active) {
        TraktBottomSheet(
            sheetState = state,
            onDismiss = onDismiss,
        ) {
            PostCommentView(
                viewModel = koinViewModel(
                    key = viewModelKey,
                    parameters = {
                        parametersOf(mediaId, mediaType, mentionSource)
                    },
                ),
                onChangesUpdate = { hasChanges = it },
                gifQuery = mediaType?.toGifQuery(mediaTitle),
                onCommentPost = {
                    onCommentPost(it)
                    hasChanges = false
                    sheetScope.dismissWithAction(
                        sheet = state,
                        onDismiss = onDismiss,
                        action = {
                            sheetScope.launch {
                                val job = sheetScope.launch {
                                    val message = localRes.getString(R.string.text_info_review_added)
                                    localSnack.showSnackbar(message)
                                }
                                delay(ShortSnackDuration)
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

        RemoveConfirmationSheet(
            active = isDiscardConfirmVisible,
            title = stringResource(R.string.dialog_title_discard_changes),
            message = stringResource(R.string.warning_prompt_discard_changes),
            onYes = {
                isDiscardConfirmVisible = false
                hasChanges = false
                sheetScope.dismissWithAction(
                    sheet = state,
                    onDismiss = onDismiss,
                )
            },
            onNo = { isDiscardConfirmVisible = false },
        )
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
