@file:OptIn(ExperimentalMaterial3Api::class)

package tv.trakt.trakt.core.comments.features.deletecomment

import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow.Companion.Ellipsis
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.ColorImage
import coil3.annotation.ExperimentalCoilApi
import coil3.compose.AsyncImagePreviewHandler
import coil3.compose.LocalAsyncImagePreviewHandler
import tv.trakt.trakt.common.helpers.LaunchedUpdateEffect
import tv.trakt.trakt.common.ui.theme.colors.Red500
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.components.buttons.PrimaryButton
import tv.trakt.trakt.ui.theme.TraktTheme

@Composable
internal fun DeleteCommentView(
    viewModel: DeleteCommentViewModel,
    modifier: Modifier = Modifier,
    onDelete: () -> Unit = {},
    onCancel: () -> Unit = {},
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedUpdateEffect(state.deleted) {
        if (state.deleted) {
            onDelete()
        }
    }

    ViewContent(
        state = state,
        modifier = modifier,
        onDeleteClick = viewModel::deleteComment,
        onCancelClick = onCancel,
    )
}

@Composable
private fun ViewContent(
    state: DeleteCommentState,
    modifier: Modifier = Modifier,
    onDeleteClick: () -> Unit,
    onCancelClick: () -> Unit,
) {
    Column(
        modifier = modifier,
    ) {
        Text(
            text = stringResource(R.string.button_text_delete_review).uppercase(),
            style = TraktTheme.typography.heading6,
            color = TraktTheme.colors.textSecondary,
            maxLines = 1,
            overflow = Ellipsis,
            modifier = Modifier
                .padding(bottom = 30.dp),
        )

        Text(
            text = stringResource(R.string.warning_prompt_delete_review),
            style = TraktTheme.typography.paragraph,
            color = TraktTheme.colors.textPrimary,
            maxLines = 5,
            overflow = Ellipsis,
        )

        Column(
            verticalArrangement = spacedBy(10.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 34.dp),
        ) {
            PrimaryButton(
                text = when {
                    state.loading.isLoading -> ""
                    else -> stringResource(R.string.button_text_yes)
                },
                enabled = !state.loading.isLoading,
                loading = state.loading.isLoading,
                onClick = onDeleteClick,
                containerColor = Red500,
                modifier = Modifier.fillMaxWidth(),
            )

            PrimaryButton(
                text = stringResource(R.string.button_text_cancel),
                containerColor = TraktTheme.colors.primaryButtonContainerDisabled,
                enabled = !state.loading.isLoading,
                onClick = onCancelClick,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@OptIn(ExperimentalCoilApi::class)
@Preview(
    device = "id:pixel_5",
    showBackground = true,
    backgroundColor = 0xFF212427,
)
@Composable
private fun Preview() {
    TraktTheme {
        val previewHandler = AsyncImagePreviewHandler {
            ColorImage(Color.Blue.toArgb())
        }
        CompositionLocalProvider(LocalAsyncImagePreviewHandler provides previewHandler) {
            ViewContent(
                state = DeleteCommentState(),
                onDeleteClick = {},
                onCancelClick = {},
            )
        }
    }
}
