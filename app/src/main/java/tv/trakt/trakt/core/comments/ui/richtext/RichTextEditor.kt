package tv.trakt.trakt.core.comments.ui.richtext

import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.util.TypedValue
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFontFamilyResolver
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import tv.trakt.trakt.common.ui.theme.colors.Purple50
import tv.trakt.trakt.common.ui.theme.colors.Red500
import tv.trakt.trakt.core.comments.model.CommentMention
import tv.trakt.trakt.helpers.extensions.TraktThemeLightDark
import tv.trakt.trakt.ui.theme.TraktTheme

private const val MAX_SUGGESTIONS = 8
private const val SPOILER_TINT_ALPHA = 0.18F
private const val DISABLED_ALPHA = 0.6F

private val SpoilerBlurRadius = 3.dp
private val BulletRadius = 3.dp
private val BlockGap = 8.dp
private val QuoteStripe = 2.dp
private val CursorWidth = 2.dp

@Composable
internal fun RichTextEditor(
    state: RichTextEditorState,
    placeholder: String,
    mentions: ImmutableList<CommentMention>,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    borderColor: Color = TraktTheme.colors.accent,
    minLines: Int = 6,
    maxLines: Int = 20,
    toolbarTrailing: @Composable () -> Unit = {},
) {
    val suggestions = remember(mentions, state.mentionQuery) {
        state.mentionQuery
            ?.let { mentions.toMentionMatches(query = it.query, limit = MAX_SUGGESTIONS) }
            .orEmpty()
    }

    Column(
        verticalArrangement = spacedBy(8.dp),
        modifier = modifier
            .border(
                width = 1.5.dp,
                color = if (state.isFocused) borderColor else TraktTheme.colors.chipContainer,
                shape = RoundedCornerShape(16.dp),
            )
            .padding(
                start = 12.dp,
                end = 12.dp,
                top = 12.dp,
                bottom = 6.dp,
            ),
    ) {
        EditorField(
            state = state,
            placeholder = placeholder,
            enabled = enabled,
            minLines = minLines,
            maxLines = maxLines,
        )

        if (suggestions.isNotEmpty()) {
            MentionList(
                mentions = suggestions,
                onPick = { state.insertMention(it, state.mentionQuery?.range) },
            )
        }

        RichTextToolbar(
            state = state,
            mentions = mentions,
            enabled = enabled,
            trailing = toolbarTrailing,
            modifier = Modifier
                .graphicsLayer {
                    translationX = -4.dp.toPx()
                },
        )
    }
}

@Composable
private fun EditorField(
    state: RichTextEditorState,
    placeholder: String,
    enabled: Boolean,
    minLines: Int,
    maxLines: Int,
) {
    val colors = TraktTheme.colors
    val textStyle = TraktTheme.typography.paragraph
    val density = LocalDensity.current
    val fontResolver = LocalFontFamilyResolver.current

    val typeface = remember(fontResolver, textStyle) {
        fontResolver.resolve(textStyle.fontFamily, textStyle.fontWeight ?: FontWeight.W400).value as? Typeface
    }
    val decorations = with(density) {
        RichTextDecorations(
            spoilerTint = Red500.copy(alpha = SPOILER_TINT_ALPHA).toArgb(),
            blurRadius = SpoilerBlurRadius.toPx(),
            bulletColor = colors.textPrimary.toArgb(),
            bulletRadius = BulletRadius.roundToPx(),
            bulletGap = BlockGap.roundToPx(),
            quoteColor = Purple50.toArgb(),
            quoteStripe = QuoteStripe.roundToPx(),
            quoteGap = BlockGap.roundToPx(),
        )
    }
    val lineHeightPx = with(density) {
        when {
            textStyle.lineHeight.isEm -> (textStyle.fontSize * textStyle.lineHeight.value).toPx()
            else -> textStyle.lineHeight.toPx()
        }
    }

    AndroidView(
        factory = { context ->
            RichTextEditText(context).apply {
                this.decorations = decorations
                setTypeface(typeface)
                setTextSize(TypedValue.COMPLEX_UNIT_SP, textStyle.fontSize.value)
                setLineHeight(lineHeightPx.toInt())
                setTextColor(colors.textPrimary.toArgb())
                setHintTextColor(colors.textSecondary.toArgb())
                highlightColor = colors.accent.copy(alpha = 0.4F).toArgb()
                setMinLines(minLines)
                setMaxLines(maxLines)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    textCursorDrawable = GradientDrawable().apply {
                        setColor(colors.textPrimary.toArgb())
                        setSize(with(density) { CursorWidth.roundToPx() }, 0)
                    }
                }
                state.attach(this)
            }
        },
        update = { view ->
            view.hint = placeholder
            view.isEnabled = enabled
            view.alpha = if (enabled) 1F else DISABLED_ALPHA
        },
        onRelease = { view -> state.detach(view) },
        modifier = Modifier.fillMaxWidth(),
    )
}

@Preview(showBackground = true, backgroundColor = 0xFF212427)
@Composable
private fun Preview() {
    TraktThemeLightDark {
        RichTextEditor(
            state = rememberRichTextEditorState(),
            placeholder = "Add a review...",
            mentions = persistentListOf(CommentMention(name = "Steve Carell", href = "")),
        )
    }
}
