@file:OptIn(ExperimentalMaterial3Api::class)

package tv.trakt.trakt.core.comments.features.editcomment

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight.Companion.W400
import androidx.compose.ui.text.style.TextOverflow.Companion.Ellipsis
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.ColorImage
import coil3.annotation.ExperimentalCoilApi
import coil3.compose.AsyncImagePreviewHandler
import coil3.compose.LocalAsyncImagePreviewHandler
import kotlinx.collections.immutable.persistentListOf
import tv.trakt.trakt.common.helpers.LaunchedUpdateEffect
import tv.trakt.trakt.common.helpers.extensions.onClick
import tv.trakt.trakt.common.helpers.preview.PreviewData
import tv.trakt.trakt.common.model.Comment
import tv.trakt.trakt.common.model.CommentGif
import tv.trakt.trakt.common.ui.theme.colors.Red400
import tv.trakt.trakt.common.ui.theme.colors.Red500
import tv.trakt.trakt.core.comments.model.CommentMention
import tv.trakt.trakt.core.comments.ui.CommentGifView
import tv.trakt.trakt.core.comments.ui.richtext.RichTextEditor
import tv.trakt.trakt.core.comments.ui.richtext.parseMarkdown
import tv.trakt.trakt.core.comments.ui.richtext.rememberRichTextEditorState
import tv.trakt.trakt.core.comments.ui.richtext.toMarkdown
import tv.trakt.trakt.core.klipy.GifPickerSheet
import tv.trakt.trakt.core.klipy.ui.SelectedGifFrame
import tv.trakt.trakt.core.klipy.ui.SelectedGifWidth
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.components.buttons.PrimaryButton
import tv.trakt.trakt.ui.components.switch.TraktSwitch
import tv.trakt.trakt.ui.theme.TraktTheme

@Composable
internal fun EditCommentView(
    viewModel: EditCommentViewModel,
    comment: Comment,
    modifier: Modifier = Modifier,
    gifQuery: String? = null,
    onCommentEdit: (Comment) -> Unit = {},
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedUpdateEffect(state.result, state.error) {
        state.result?.let {
            onCommentEdit(it)
        }
    }

    ViewContent(
        state = state,
        comment = comment,
        gifQuery = gifQuery,
        onSubmitClick = { text, spoiler, gif ->
            viewModel.submitComment(
                text = text,
                spoiler = spoiler,
                gif = gif,
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
    state: EditCommentState,
    comment: Comment,
    modifier: Modifier = Modifier,
    gifQuery: String? = null,
    onSubmitClick: (text: String, spoiler: Boolean, gif: CommentGif?) -> Unit,
    onErrorClick: () -> Unit,
) {
    val focusManager = LocalFocusManager.current
    val editorState = rememberRichTextEditorState()
    val isLoading = state.loading.isLoading

    var isSpoiler by remember { mutableStateOf(comment.isSpoiler) }
    var isGifPickerVisible by remember { mutableStateOf(false) }
    var selectedGif by remember { mutableStateOf(comment.gif) }

    // Compared against the editor output, so markdown written elsewhere does not count as a change.
    val initialMarkdown = remember(comment.comment) { comment.comment.parseMarkdown().toMarkdown() }

    LaunchedEffect(comment.comment) {
        editorState.setMarkdown(comment.comment)
    }

    // A GIF is content on its own, so the minimum word count applies to text-only comments.
    val isValid = remember {
        val spaceRegex = "\\s+".toRegex()
        derivedStateOf {
            if (selectedGif != null) return@derivedStateOf true

            val input = editorState.text.trim()
            input.isNotBlank() && input.split(spaceRegex).size >= 5
        }
    }

    val isNotEmpty = remember {
        derivedStateOf {
            val input = editorState.text.trim()
            input.isNotBlank()
        }
    }

    val isChanged = remember {
        derivedStateOf {
            editorState.markdown != initialMarkdown ||
                isSpoiler != comment.isSpoiler ||
                selectedGif != comment.gif
        }
    }

    Column(
        verticalArrangement = spacedBy(0.dp),
        modifier = modifier,
    ) {
        Row(
            verticalAlignment = Alignment.Bottom,
            modifier = Modifier
                .fillMaxWidth()
                .animateContentSize(),
        ) {
            RichTextEditor(
                state = editorState,
                enabled = !isLoading,
                placeholder = stringResource(R.string.textarea_placeholder_comment),
                mentions = state.mentions,
                borderColor = when {
                    isNotEmpty.value && !isValid.value -> Red400
                    else -> TraktTheme.colors.accent
                },
                modifier = Modifier.weight(1F),
                toolbarTrailing = {
                    if (selectedGif == null) {
                        Icon(
                            painter = painterResource(R.drawable.ic_gif),
                            contentDescription = stringResource(R.string.button_label_add_gif),
                            tint = TraktTheme.colors.textPrimary,
                            modifier = Modifier
                                .graphicsLayer {
                                    translationX = 4.dp.toPx()
                                }
                                .size(22.dp)
                                .onClick(enabled = !isLoading) {
                                    focusManager.clearFocus()
                                    isGifPickerVisible = true
                                },
                        )
                    }
                },
            )

            AnimatedContent(
                targetState = selectedGif,
                contentKey = { it?.url },
                transitionSpec = {
                    (fadeIn() + expandHorizontally(expandFrom = Alignment.Start))
                        .togetherWith(fadeOut() + shrinkHorizontally(shrinkTowards = Alignment.Start))
                        .using(SizeTransform(clip = false))
                },
                label = "selectedGif",
            ) { gif ->
                if (gif != null) {
                    SelectedGifFrame(
                        enabled = !isLoading,
                        onRemoveClick = { selectedGif = null },
                        modifier = Modifier
                            .padding(start = 12.dp)
                            .width(SelectedGifWidth),
                    ) {
                        CommentGifView(
                            gif = gif,
                        )
                    }
                }
            }
        }

        GifPickerSheet(
            visible = isGifPickerVisible,
            defaultQuery = gifQuery,
            onGifSelected = { gif -> gif.toCommentGif()?.let { selectedGif = it } },
            onDismiss = { isGifPickerVisible = false },
        )

        if (selectedGif == null) {
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
        }

        if (state.error != null) {
            Text(
                text = state.error.message
                    ?: stringResource(R.string.error_text_unexpected_error_short),
                color = Red500,
                style = TraktTheme.typography.paragraphSmaller,
                maxLines = 3,
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
                top = when {
                    selectedGif == null -> 0.dp
                    else -> 15.dp
                },
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
            text = stringResource(R.string.button_text_edit_comment),
            enabled = !isLoading && isValid.value && isChanged.value,
            loading = isLoading,
            onClick = {
                onSubmitClick(
                    editorState.markdown,
                    isSpoiler,
                    selectedGif,
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
            Column(verticalArrangement = spacedBy(32.dp)) {
                ViewContent(
                    state = EditCommentState(
                        mentions = persistentListOf(
                            CommentMention(name = "Steve Carell", href = "", detail = "Michael Scott"),
                        ),
                    ),
                    comment = PreviewData.comment1.copy(
                        comment = "**Great** movie with a *twist*.\n\n- [spoiler]He was dead[/spoiler]\n> Quote",
                    ),
                    onSubmitClick = { _, _, _ -> },
                    onErrorClick = { },
                )
                ViewContent(
                    state = EditCommentState(),
                    comment = PreviewData.comment1.copy(
                        gif = CommentGif(
                            slug = "funny-cat",
                            url = "https://example.com/gif.gif",
                        ),
                    ),
                    onSubmitClick = { _, _, _ -> },
                    onErrorClick = { },
                )
            }
        }
    }
}
