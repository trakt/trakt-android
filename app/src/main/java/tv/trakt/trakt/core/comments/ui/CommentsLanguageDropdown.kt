package tv.trakt.trakt.core.comments.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import tv.trakt.trakt.common.helpers.extensions.onClick
import tv.trakt.trakt.core.comments.model.commentsLanguages
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.theme.TraktTheme

private val FlagSize = 16.dp
private val CheckSize = 14.dp

@Composable
internal fun CommentsLanguageDropdown(
    language: String?,
    modifier: Modifier = Modifier,
    iconSize: Dp = 18.dp,
    enabled: Boolean = true,
    onLanguageClick: ((String?) -> Unit)? = null,
) {
    val languages = remember { commentsLanguages() }
    var showMenu by remember { mutableStateOf(false) }

    Box(modifier = modifier) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(iconSize)
                .onClick(enabled = enabled) {
                    showMenu = true
                },
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_world),
                contentDescription = null,
                tint = TraktTheme.colors.textPrimary,
                modifier = Modifier.size(iconSize),
            )

            if (language != null) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .graphicsLayer {
                            translationX = -1.dp.toPx()
                            translationY = -1.dp.toPx()
                        }
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(TraktTheme.colors.accent),
                )
            }
        }

        DropdownMenu(
            expanded = showMenu,
            containerColor = TraktTheme.colors.dropdownMenuContainer,
            shape = RoundedCornerShape(16.dp),
            offset = DpOffset(0.dp, 4.dp),
            onDismissRequest = {
                showMenu = false
            },
        ) {
            LanguageItem(
                text = stringResource(R.string.option_text_comment_language_all),
                selected = language == null,
                onClick = {
                    showMenu = false
                    onLanguageClick?.invoke(null)
                },
            )

            for (option in languages) {
                LanguageItem(
                    text = option.displayName,
                    selected = language == option.code,
                    leadingContent = when (option.flag) {
                        null -> {
                            null
                        }
                        else -> {
                            {
                                Text(
                                    text = option.flag,
                                    style = TraktTheme.typography.buttonTertiary,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.width(FlagSize),
                                )
                            }
                        }
                    },
                    onClick = {
                        showMenu = false
                        onLanguageClick?.invoke(option.code)
                    },
                )
            }
        }
    }
}

@Composable
private fun LanguageItem(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    leadingContent: @Composable (() -> Unit)? = null,
) {
    DropdownMenuItem(
        text = {
            Row(
                horizontalArrangement = spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (selected) {
                    Icon(
                        painter = painterResource(R.drawable.ic_check),
                        contentDescription = null,
                        tint = TraktTheme.colors.textPrimary,
                        modifier = Modifier.size(CheckSize),
                    )
                }

                leadingContent?.let {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.width(FlagSize),
                    ) {
                        it.invoke()
                    }
                }

                Text(
                    text = text,
                    style = TraktTheme.typography.buttonTertiary,
                    color = TraktTheme.colors.textPrimary,
                )
            }
        },
        onClick = onClick,
    )
}

@Preview
@Composable
private fun CommentsLanguageDropdownPreview() {
    TraktTheme {
        Row(
            horizontalArrangement = spacedBy(16.dp),
            modifier = Modifier.padding(16.dp),
        ) {
            CommentsLanguageDropdown(language = null)
            CommentsLanguageDropdown(language = "de")
        }
    }
}
