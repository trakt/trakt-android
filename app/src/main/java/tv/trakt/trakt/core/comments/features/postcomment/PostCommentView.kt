@file:OptIn(ExperimentalMaterial3Api::class)

package tv.trakt.trakt.core.comments.features.postcomment

import InputField
import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.ColorImage
import coil3.annotation.ExperimentalCoilApi
import coil3.compose.AsyncImagePreviewHandler
import coil3.compose.LocalAsyncImagePreviewHandler
import tv.trakt.trakt.common.helpers.LaunchedUpdateEffect
import tv.trakt.trakt.common.model.Comment
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.components.TraktHeader
import tv.trakt.trakt.ui.components.buttons.PrimaryButton
import tv.trakt.trakt.ui.theme.TraktTheme

@Composable
internal fun PostCommentView(
    viewModel: PostCommentViewModel,
    modifier: Modifier = Modifier,
    onCommentPost: (Comment) -> Unit = {},
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedUpdateEffect(state.result) {
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
        modifier = modifier,
    )
}

@Composable
private fun ViewContent(
    state: PostCommentState,
    modifier: Modifier = Modifier,
    onSubmitClick: (comment: String, spoiler: Boolean) -> Unit,
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

        PrimaryButton(
            text = stringResource(R.string.button_text_submit),
            enabled = !isLoading && isValid.value,
            loading = isLoading,
            onClick = {
                val input = inputState.text
                    .toString()
                    .trim()
                onSubmitClick(input, false)
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 29.dp),
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
            )
        }
    }
}
