package tv.trakt.trakt.core.comments.ui.richtext

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.input.clearText
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.isTraversalGroup
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import tv.trakt.trakt.common.helpers.extensions.onClick
import tv.trakt.trakt.core.comments.model.CommentMention
import tv.trakt.trakt.helpers.extensions.TraktThemeLightDark
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.components.InputField
import tv.trakt.trakt.ui.theme.TraktTheme

private const val MAX_MENTION_MATCHES = 20

private val ToolbarButtonSize = 30.dp
private val ToolbarIconSize = 18.dp

@Composable
internal fun RichTextToolbar(
    state: RichTextEditorState,
    mentions: List<CommentMention>,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    trailing: @Composable () -> Unit = {},
) {
    var isPickerOpen by remember { mutableStateOf(false) }

    Column(
        verticalArrangement = spacedBy(4.dp),
        modifier = modifier.fillMaxWidth(),
    ) {
        if (isPickerOpen) {
            MentionPicker(
                mentions = mentions,
                onPick = {
                    isPickerOpen = false
                    state.insertMention(it)
                },
                onClose = {
                    isPickerOpen = false
                    state.focus()
                },
            )
            return@Column
        }

        FormattingRow(
            toolbar = state.toolbar,
            hasMentions = mentions.isNotEmpty(),
            enabled = enabled,
            onInline = state::toggleInline,
            onBlock = state::toggleBlock,
            onMention = {
                if (state.toolbar.mention) {
                    state.removeMention()
                } else {
                    isPickerOpen = true
                }
            },
            trailing = trailing,
        )
    }
}

@Composable
private fun FormattingRow(
    toolbar: RichToolbarState,
    hasMentions: Boolean,
    enabled: Boolean,
    onInline: (RichInlineStyle) -> Unit,
    onBlock: (RichBlockType) -> Unit,
    onMention: () -> Unit,
    trailing: @Composable () -> Unit,
) {
    val toolbarLabel = stringResource(R.string.toolbar_label_text_formatting)

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = spacedBy(4.dp),
        modifier = Modifier
            .fillMaxWidth()
            .semantics {
                contentDescription = toolbarLabel
                isTraversalGroup = true
            },
    ) {
        ToolbarButton(
            icon = R.drawable.ic_format_bold,
            label = stringResource(R.string.button_label_format_bold),
            active = toolbar.bold,
            enabled = enabled,
            onClick = { onInline(RichInlineStyle.Bold) },
        )
        ToolbarButton(
            icon = R.drawable.ic_format_italic,
            label = stringResource(R.string.button_label_format_italic),
            active = toolbar.italic,
            enabled = enabled,
            onClick = { onInline(RichInlineStyle.Italic) },
        )
        ToolbarButton(
            icon = R.drawable.ic_eye_off,
            label = stringResource(R.string.button_label_format_spoiler),
            active = toolbar.spoiler,
            enabled = enabled,
            onClick = { onInline(RichInlineStyle.Spoiler) },
        )
        ToolbarButton(
            icon = R.drawable.ic_format_bullet_list,
            label = stringResource(R.string.button_label_format_bullet_list),
            active = toolbar.bulletList,
            enabled = enabled,
            onClick = { onBlock(RichBlockType.Bullet) },
        )
        ToolbarButton(
            icon = R.drawable.ic_format_quote,
            label = stringResource(R.string.button_label_format_quote),
            active = toolbar.quote,
            enabled = enabled,
            onClick = { onBlock(RichBlockType.Quote) },
        )

        if (hasMentions) {
            ToolbarButton(
                icon = R.drawable.ic_mention,
                label = when {
                    toolbar.mention -> stringResource(R.string.button_label_format_remove_mention)
                    else -> stringResource(R.string.button_label_format_mention)
                },
                active = toolbar.mention,
                enabled = enabled,
                onClick = onMention,
            )
        }

        Spacer(Modifier.weight(1F))
        trailing()
    }
}

@Composable
private fun MentionPicker(
    mentions: List<CommentMention>,
    onPick: (CommentMention) -> Unit,
    onClose: () -> Unit,
) {
    val searchState = rememberTextFieldState()
    val focusRequester = remember { FocusRequester() }
    val matches = mentions.toMentionMatches(
        query = searchState.text.toString(),
        limit = MAX_MENTION_MATCHES,
    )

    LaunchedEffect(Unit) {
        searchState.clearText()
        focusRequester.requestFocus()
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = spacedBy(4.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        InputField(
            state = searchState,
            placeholder = stringResource(R.string.input_placeholder_mention_search),
            height = 40.dp,
            modifier = Modifier
                .weight(1F)
                .focusRequester(focusRequester),
        )
        ToolbarButton(
            icon = R.drawable.ic_close,
            label = stringResource(R.string.button_label_cancel),
            active = false,
            enabled = true,
            onClick = onClose,
        )
    }

    if (matches.isEmpty()) {
        Text(
            text = stringResource(R.string.text_no_matching_cast),
            color = TraktTheme.colors.textSecondary,
            style = TraktTheme.typography.meta,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
        )
        return
    }

    MentionList(mentions = matches, onPick = onPick)
}

@Composable
private fun ToolbarButton(
    @DrawableRes icon: Int,
    label: String,
    active: Boolean,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    val accent = TraktTheme.colors.accent

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(ToolbarButtonSize)
            .clip(RoundedCornerShape(8.dp))
            .background(if (active) accent.copy(alpha = 0.2F) else Color.Transparent)
            .onClick(enabled = enabled, throttle = false, indication = true, onClick = onClick)
            .semantics {
                role = Role.Button
                selected = active
            },
    ) {
        Icon(
            painter = painterResource(icon),
            contentDescription = label,
            tint = if (active) accent else TraktTheme.colors.textPrimary,
            modifier = Modifier.size(ToolbarIconSize),
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF212427)
@Composable
private fun Preview() {
    TraktThemeLightDark {
        FormattingRow(
            toolbar = RichToolbarState(bold = true, quote = true),
            hasMentions = true,
            enabled = true,
            onInline = {},
            onBlock = {},
            onMention = {},
            trailing = {},
        )
    }
}
