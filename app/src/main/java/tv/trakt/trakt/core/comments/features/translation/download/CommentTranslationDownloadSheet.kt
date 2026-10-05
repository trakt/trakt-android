@file:OptIn(ExperimentalMaterial3Api::class)

package tv.trakt.trakt.core.comments.features.translation.download

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SheetState
import androidx.compose.material3.SheetValue.Expanded
import androidx.compose.material3.SheetValue.Hidden
import androidx.compose.material3.rememberBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.androidx.compose.koinViewModel
import tv.trakt.trakt.core.comments.features.translation.model.CommentTranslationEvent
import tv.trakt.trakt.core.comments.features.translation.ui.openExternalTranslation
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.components.confirmation.ConfirmationSheet

@Composable
internal fun CommentTranslationDownloadSheet(
    viewModel: CommentTranslationDownloadViewModel = koinViewModel(),
    state: SheetState = rememberBottomSheetState(
        initialValue = Hidden,
        enabledValues = setOf(Hidden, Expanded),
    ),
) {
    val context = LocalContext.current
    val downloadState by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is CommentTranslationEvent.OpenExternalTranslation -> {
                    context.openExternalTranslation(event.text)
                }
            }
        }
    }

    ConfirmationSheet(
        state = state,
        active = downloadState.comment != null,
        title = stringResource(R.string.header_comment_translation_download),
        message = stringResource(R.string.text_comment_translation_download_metered),
        yesText = stringResource(R.string.button_text_continue),
        onYes = {
            downloadState.comment?.let(viewModel::confirmDownload)
        },
        onNo = viewModel::cancelDownload,
    )
}
