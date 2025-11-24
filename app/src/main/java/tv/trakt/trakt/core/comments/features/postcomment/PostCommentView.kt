@file:OptIn(ExperimentalMaterial3Api::class)

package tv.trakt.trakt.core.comments.features.postcomment

import InputField
import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
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
import tv.trakt.trakt.common.helpers.extensions.onClick
import tv.trakt.trakt.common.model.Comment
import tv.trakt.trakt.common.ui.theme.colors.Red500
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.components.TraktHeader
import tv.trakt.trakt.ui.components.TraktSwitch
import tv.trakt.trakt.ui.components.buttons.PrimaryButton
import tv.trakt.trakt.ui.theme.TraktTheme

@Composable
internal fun PostCommentView(
    viewModel: PostCommentViewModel,
    modifier: Modifier = Modifier,
    onCommentPost: (Comment) -> Unit = {},
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedUpdateEffect(state.result, state.error) {
        state.result?.let {
            onCommentPost(it)
        }
    }

    ViewContent(
        state = state,
        onSubmitClick = { comment, spoiler ->
            viewModel.submitComment(
                comment = comment,
                spoiler = spoiler,
            )
        },
        onErrorClick = {
            viewModel.clearError()
        },
        modifier = modifier,
    )
}

@Composable
private fun ViewContent(
    state: PostCommentState,
    modifier: Modifier = Modifier,
    onSubmitClick: (comment: String, spoiler: Boolean) -> Unit,
    onErrorClick: () -> Unit,
) {
    val inputState = rememberTextFieldState()
    val isLoading = state.loading.isLoading

    val isValid = remember {
        val spaceRegex = "\\s+".toRegex()
        derivedStateOf {
            val input = inputState.text.toString().trim()
            input.isNotBlank() && input.split(spaceRegex).size >= 5
        }
    }

    var isSpoiler by remember { mutableStateOf(false) }

    Column(
        verticalArrangement = spacedBy(0.dp),
        modifier = modifier,
    ) {
        TraktHeader(
            title = stringResource(R.string.button_text_comment),
            subtitle = stringResource(R.string.page_description_add_comment),
        )

        InputField(
            state = inputState,
            enabled = !isLoading,
            placeholder = stringResource(R.string.input_placeholder_comment),
            containerColor = Color.Transparent,
            height = 164.dp,
            lineLimits = TextFieldLineLimits.MultiLine(
                minHeightInLines = 3,
                maxHeightInLines = Int.MAX_VALUE,
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 24.dp),
        )

        if (state.error != null) {
            Text(
                text = state.error.message
                    ?: stringResource(R.string.error_text_unexpected_error_short),
                color = Red500,
                style = TraktTheme.typography.paragraphSmaller,
                maxLines = 10,
                overflow = Ellipsis,
                modifier = Modifier
                    .padding(top = 26.dp)
                    .onClick(onClick = onErrorClick),
            )
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = spacedBy(16.dp),
            modifier = Modifier.padding(vertical = 12.dp),
        ) {
            Text(
                text = stringResource(R.string.button_text_spoilers),
                color = TraktTheme.colors.textPrimary,
                style = TraktTheme.typography.paragraph,
                maxLines = 1,
                overflow = Ellipsis,
            )

            TraktSwitch(
                checked = isSpoiler,
                onCheckedChange = { isSpoiler = it },
                enabled = !isLoading,
            )
        }

        PrimaryButton(
            text = stringResource(R.string.button_text_submit),
            enabled = !isLoading && isValid.value,
            loading = isLoading,
            onClick = {
                val input = inputState.text
                    .toString()
                    .trim()

                onSubmitClick(
                    input,
                    isSpoiler,
                )
            },
            modifier = Modifier.fillMaxWidth(),
        )
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
                state = PostCommentState(),
                onSubmitClick = { _, _ -> },
                onErrorClick = { },
            )
        }
    }
}
