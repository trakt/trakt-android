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
import androidx.compose.ui.text.font.FontWeight.Companion.W400
import androidx.compose.ui.text.input.ImeAction
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
import tv.trakt.trakt.common.ui.theme.colors.Red400
import tv.trakt.trakt.common.ui.theme.colors.Red500
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.components.buttons.PrimaryButton
import tv.trakt.trakt.ui.components.switch.TraktSwitch
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

    val isNotEmpty = remember {
        derivedStateOf {
            val input = inputState.text.toString().trim()
            input.isNotBlank()
        }
    }

    var isSpoiler by remember { mutableStateOf(false) }

    Column(
        verticalArrangement = spacedBy(0.dp),
        modifier = modifier,
    ) {
        InputField(
            state = inputState,
            enabled = !isLoading,
            placeholder = stringResource(R.string.textarea_placeholder_comment),
            containerColor = Color.Transparent,
            borderColor = when {
                isNotEmpty.value && !isValid.value -> Red400
                else -> TraktTheme.colors.accent
            },
            lineLimits = TextFieldLineLimits.MultiLine(
                minHeightInLines = 5,
                maxHeightInLines = Int.MAX_VALUE,
            ),
            imeAction = ImeAction.Default,
            modifier = Modifier.fillMaxWidth(),
        )

        Text(
            text = stringResource(R.string.translated_value_error_comment_invalid_content),
            color = when {
                isNotEmpty.value && !isValid.value -> Red400
                else -> TraktTheme.colors.textSecondary
            },
            style = TraktTheme.typography.meta.copy(fontWeight = W400),
            maxLines = 1,
            overflow = Ellipsis,
            modifier = Modifier
                .align(Alignment.End)
                .padding(top = 4.dp, end = 10.dp),
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
            horizontalArrangement = spacedBy(12.dp),
            modifier = Modifier.padding(
                top = 0.dp,
                bottom = 17.dp,
            ),
        ) {
            Text(
                text = stringResource(R.string.text_spoiler),
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
            text = stringResource(R.string.button_text_add_review),
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
